package checkers.fun.jimuva;

import checkers.basetype.BaseTypeVisitor;
import checkers.fun.quals.Function;
import checkers.source.Result;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedDeclaredType;
import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;
import checkers.util.ElementUtils;
import checkers.util.InternalUtils;
import checkers.util.TreeUtils;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.tools.Diagnostic.Kind;

/**
 *
 * @author saf
 */
public class JimuvaVisitor extends BaseTypeVisitor<Void, Void> {

    protected JimuvaChecker checker;

    protected JimuvaAnnotatedTypeFactory atypeFactory;
    protected JimuvaVisitorState state;

    public JimuvaVisitor(JimuvaChecker checker, CompilationUnitTree root) throws IOException {
        super(checker, root);
        this.checker = checker;
        atypeFactory = new JimuvaAnnotatedTypeFactory(checker, root);
        state = new JimuvaVisitorState(atypeFactory);
    }

    @Override
    public Void visitAssignment(AssignmentTree node, Void p) {
        ExpressionTree varTree = node.getVariable();

        AnnotatedTypeMirror receiver = atypeFactory.getReceiver(varTree);
        //System.err.println("Receiver type is " + receiver.toString());
        if (receiver != null && receiver.hasAnnotation(checker.IMMUTABLE)) {
            String messageKey = TreeUtils.isSelfAccess(varTree)
                    ? "readonly.assigns.receiver.field"
                    : "assigning.field.of.immutable";
            checker.report(Result.failure(messageKey), varTree);
        }
        AnnotatedTypeMirror methodReceiver = state.getCurrentMethod().getReceiverType();
        checkAssignmentRep(node, methodReceiver != null
                && methodReceiver.hasAnnotation(checker.IMMUTABLE));

        if (state.isCurrentMethod(checker.ANONYMOUS)) {
            checkAssignmentAnonymous(node);
        }

        if (mayBeThis(node.getExpression())) {
            /* #TODO this may err if a method enclosed in another method
             * hides the enclosing method's local variable. */
            try {
                Element varElement = TreeUtils.elementFromUse(node.getVariable());
                state.addThisAlias(varElement);
            } catch (IllegalArgumentException e) {
                /* The node was not an element use. Swallow the exception. */
            }
        }

        return super.visitAssignment(node, p);
    }

    @Override
    protected void commonAssignmentCheck(AnnotatedTypeMirror varType, AnnotatedTypeMirror valueType, Tree valueTree, String errorKey, Void p) {
        // System.err.println("Assigning " + valueTree.toString() + " (" + valueType.toString() +
        //        ") to " + varType.toString());

        /* An object cannot lose or gain the @Rep annotation */
        if (varType.hasAnnotation(checker.REP) 
                && !valueType.hasAnnotation(checker.REP)) {
            checker.report(Result.failure(errorKey, valueType.toString(), varType.toString()), valueTree);
        } else if (!varType.hasAnnotation(checker.REP)
                && valueType.hasAnnotation(checker.REP)) {
            checker.report(Result.failure(errorKey, valueType.toString(), varType.toString()), valueTree);
        }

        /* An object cannot lose or gain the @Immutable annotation */
        if (varType.hasAnnotation(checker.MUTABLE)
                && valueType.hasAnnotation(checker.IMMUTABLE)) {
            checker.report(Result.failure(errorKey, valueType.toString(), varType.toString()), valueTree);
        } else if (varType.hasAnnotation(checker.IMMUTABLE)
                && valueType.hasAnnotation(checker.MUTABLE)) {
            checker.report(Result.failure(errorKey, valueType.toString(), varType.toString()), valueTree);
        } else {
            super.commonAssignmentCheck(varType, valueType, valueTree, errorKey, p);
        }
    }

    @Override
    public Void visitClass(ClassTree node, Void p) {
        state.enterClass(node);
        try {
            return super.visitClass(node, p);
        } finally {
            state.leaveClass();
        }
    }

    @Override
    public Void visitMethod(MethodTree node, Void p) {
        state.enterMethod(node);
        try {
            return super.visitMethod(node, p);
        } finally {
            state.leaveMethod();
        }
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
        AnnotatedExecutableType calledMethod = atypeFactory.methodFromUse(node);
        // System.err.println("INVOCATION: " + node.toString());
        AnnotatedTypeMirror receiver = atypeFactory.getReceiver(node);
        AnnotatedTypeMirror currentMethodReceiver = state.getCurrentMethod().getReceiverType();
        AnnotatedTypeMirror calledMethodReceiver = calledMethod.getReceiverType();


        /* Method calls cannot alter the inner representation of @Immutable objects */
        if (state.isCurrentMethod(checker.IMMUTABLE)
                && receiver != null && receiver.hasAnnotation(checker.REP)
                && calledMethodReceiver != null && !calledMethodReceiver.hasAnnotation(checker.IMMUTABLE)) {
            checker.report(Result.failure("nonreadonly.call.on.rep"), node);
        }

        if (state.isCurrentMethod(checker.ANONYMOUS)) {
            checkCallAnonymous(node);
        }

        return super.visitMethodInvocation(node, p);
    }

    @Override
    public Void visitReturn(ReturnTree node, Void p) {
        if (state.isCurrentMethod(checker.ANONYMOUS)
                && mayBeThis(node.getExpression())) {
            checker.report(Result.failure("anonymous.returns.this"), node);
        }
        return super.visitReturn(node, p);
    }



    @Override
    protected boolean checkMethodInvocability(AnnotatedExecutableType method, MethodInvocationTree node) {
        AnnotatedExecutableType mcp = (AnnotatedExecutableType) annoTypes.deepCopy(method);
        AnnotatedTypeMirror methodReceiver = mcp.getReceiverType().getErased();
        AnnotatedTypeMirror treeReceiver = methodReceiver.getCopy(false);
        treeReceiver.addAnnotations(atypeFactory.getReceiver(node).getAnnotations());

        /* Allow for @ReadOnly calls on @Mutable references. */
        if (treeReceiver.hasAnnotation(checker.MUTABLE)
                && methodReceiver.hasAnnotation(checker.IMMUTABLE)) {
            mcp.getReceiverType().removeAnnotation(checker.IMMUTABLE);
            mcp.getReceiverType().addAnnotation(checker.MUTABLE);
        }
        return super.checkMethodInvocability(mcp, node);
    }

    /**
     * Check that an assignment does not modify @Rep objects.
     */
    protected void checkAssignmentRep(AssignmentTree node, boolean receiverImmutable) {
        ExpressionTree varTree = node.getVariable();
        if (varTree instanceof ArrayAccessTree) {
            ArrayAccessTree t = (ArrayAccessTree) varTree;
            boolean dig = true;
            boolean rep = false;
            while (dig) {
                ExpressionTree e = t.getExpression();
                AnnotatedTypeMirror et = atypeFactory.getAnnotatedType(e);
                System.err.println("ARRAY: " + e.toString() + " : " + et.toString());
                if (et.hasAnnotation(checker.IMMUTABLE)) {
                    String errorKey = rep
                            ? "assignment.to.rep.array.of.immutable"
                            : "assignment.to.field.of.immutable.array";
                    checker.report(Result.failure(errorKey, e.toString()), varTree);
                    dig = false;
                } else if (!(e instanceof ArrayAccessTree)) {
//                    System.err.println((et.hasAnnotation(checker.REP) ? "yes " : " no")
//                            + (TreeUtils.isSelfAccess(e) ? " yes" : " no")
//                            + (receiverImmutable ? " yes" : " no"));
                    if (et.hasAnnotation(checker.REP)
                            && TreeUtils.isSelfAccess(e)
                            && receiverImmutable) {
                        String errorKey = rep
                                ? "assignment.to.rep.array.of.rep.array.receiver"
                                : "assignment.to.rep.array.of.receiver";
                        checker.report(Result.failure(errorKey, e.toString()), varTree);
                    }
                    dig = false;
                } else if (et.hasAnnotation(checker.REP)) {
                    rep = true;
                    t = (ArrayAccessTree) e;
                }
            };
        }
    }

    /**
     * Check an assignment within an @Anonymous method.
     * @param node
     */
    protected void checkAssignmentAnonymous(AssignmentTree node) {
        ExpressionTree ex = node.getExpression();
        ExpressionTree var = node.getVariable();
        if (mayBeThis(ex)) {
            try {
                Element varElement = TreeUtils.elementFromUse(var);
                if (varElement.getKind() == ElementKind.FIELD) {
                    checker.report(Result.failure("anonymous.assigns.this.to.field"), var);
                }
            } catch (IllegalArgumentException e) {
                /* Tree is not an element use */
                if (var.getKind() == Tree.Kind.ARRAY_ACCESS) {
                    checker.report(Result.failure("anonymous.assigns.this.to.array.field"), var);
                }
            }
        }
    }

    /**
     * Check a function call inside an @Anonymous method.
     * @param node
     */
    protected void checkCallAnonymous(MethodInvocationTree node) {
        /* Check that [this] is not passed as an argument */
        for (ExpressionTree arg : node.getArguments()) {
            String exThis = checkNotThis(arg);
            if (exThis != null) {
                /* #TODO more meaningful error message */
                checker.report(Result.failure("argument.may.be.this", exThis), arg);
            }
        }

        /* Check that non-@Anonymous methods are not called on [this] */
        AnnotatedExecutableType method = atypeFactory.methodFromUse(node);
        if (!method.hasAnnotation(checker.ANONYMOUS)
                    && !isBaseConstructorCall(node)) {
            if (TreeUtils.isSelfAccess(node)) {
                checker.report(Result.failure("anonymous.calls.non.anonymous"), node);
            } else {
                ExpressionTree select = node.getMethodSelect();
                if (select.getKind() == Tree.Kind.MEMBER_SELECT) {
                    MemberSelectTree selTree = (MemberSelectTree) select;
                    if (mayBeThis(selTree)) {
                        checker.report(Result.failure("anonymous.calls.non.anonymous.on.alias"), node);
                    }
                }
            }
        }
    }

    /**
     * @param node a method call tree
     * @return true if the called method is the base constructor Object()
     */
    protected boolean isBaseConstructorCall(MethodInvocationTree node) {
        AnnotatedExecutableType method = atypeFactory.methodFromUse(node);
        ExecutableElement methodElement = method.getElement();
        //System.err.println("Base? " + methodElement.getEnclosingElement().getSimpleName().toString()
        //        + " " + methodElement.getSimpleName());
        if (methodElement.getEnclosingElement().getSimpleName().contentEquals("Object")
                && methodElement.getSimpleName().contentEquals("<init>")) {
            return true;
        }
        return false;
    }

    /**
     * Return an error message key if the expression may evaluate to a reference to [this].
     * @param node the [ExpressionTree] to be checked.
     */
    protected String checkNotThis(ExpressionTree node) {
        if (node.getKind() == Tree.Kind.IDENTIFIER) {
            IdentifierTree tree = (IdentifierTree) node;
            Element sym = InternalUtils.symbol(tree);
            if (state.isThisAlias(sym)) {
                /* #TODO Info on where the alias is assigned */
                return "local.may.be.alias.to.this";
            } else if (tree.getName().contentEquals("this")) {
                return "this.identifier";
            }
        } else if (node.getKind() == Tree.Kind.METHOD_INVOCATION) {
            MethodInvocationTree tree = (MethodInvocationTree) node;
            AnnotatedExecutableType method = atypeFactory.methodFromUse(tree);
            if (!method.hasAnnotation(checker.ANONYMOUS)) {
                if (TreeUtils.isSelfAccess(node)) {
                    return "call.to.non.anonymous.method.on.this";
                } else {
                    ExpressionTree select = tree.getMethodSelect();
                    if (select.getKind() == Tree.Kind.MEMBER_SELECT) {
                        MemberSelectTree selTree = (MemberSelectTree) select;
                        String selError = checkNotThis(selTree.getExpression());
                        if (selError != null) {
                            checker.report(Result.failure("call.to.non.anonymous.on.this.alias"), node);
                        }
                    }
                }
            }
        } 
        return null;
    }

    protected Boolean mayBeThis(ExpressionTree node) {
        return checkNotThis(node) != null;
    }
}
