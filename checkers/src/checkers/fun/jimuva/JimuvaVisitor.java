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
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;

/**
 *
 * @author saf
 */
public class JimuvaVisitor extends BaseTypeVisitor<Void, Void> {

    protected JimuvaChecker checker;

    protected AnnotatedExecutableType currentMethod;
    protected AnnotatedDeclaredType currentClass;

    protected JimuvaAnnotatedTypeFactory atypeFactory;

    public JimuvaVisitor(JimuvaChecker checker, CompilationUnitTree root) throws IOException {
        super(checker, root);
        this.checker = checker;
        atypeFactory = new JimuvaAnnotatedTypeFactory(checker, root);
    }

    @Override
    public Void visitAssignment(AssignmentTree node, Void p) {
        System.err.println(node.toString());
        ExpressionTree varTree = node.getVariable();

        AnnotatedTypeMirror receiver = atypeFactory.getReceiver(varTree);
        //System.err.println("Receiver type is " + receiver.toString());
        if (receiver != null && receiver.hasAnnotation(checker.IMMUTABLE)) {
            String messageKey = TreeUtils.isSelfAccess(varTree)
                    ? "readonly.assigns.receiver.field"
                    : "assigning.field.of.immutable";
            checker.report(Result.failure(messageKey), varTree);
        }
        AnnotatedTypeMirror methodReceiver = currentMethod.getReceiverType();
        checkAssignmentRep(node, methodReceiver != null
                && methodReceiver.hasAnnotation(checker.IMMUTABLE));

        if (currentMethod.hasAnnotation(checker.ANONYMOUS)) {
            checkAssignmentAnonymous(node);
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
        currentClass = atypeFactory.getAnnotatedType(node);
        return super.visitClass(node, p);
    }

    @Override
    public Void visitMethod(MethodTree node, Void p) {
        currentMethod = atypeFactory.getAnnotatedType(node);
        System.err.println("Method " + node.toString() + "\n  has type " + currentMethod.toString());
        return super.visitMethod(node, p);
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
        AnnotatedExecutableType calledMethod = atypeFactory.methodFromUse(node);
        //System.err.println("Invocation " + node.toString());
        AnnotatedTypeMirror receiver = atypeFactory.getReceiver(node);
        AnnotatedTypeMirror currentMethodReceiver = currentMethod.getReceiverType();
        AnnotatedTypeMirror calledMethodReceiver = calledMethod.getReceiverType();


        /* Method calls cannot alter the inner representation of @Immutable objects */
        if (currentMethod != null && currentMethod.hasAnnotation(checker.IMMUTABLE)
                && receiver != null && receiver.hasAnnotation(checker.REP)
                && calledMethodReceiver != null && !calledMethodReceiver.hasAnnotation(checker.IMMUTABLE)) {
            checker.report(Result.failure("nonreadonly.call.on.rep"), node);
        }

        if (currentMethod.hasAnnotation(checker.ANONYMOUS)) {
            checkCallAnonymous(node);
        }

        return super.visitMethodInvocation(node, p);
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
                    System.err.println((et.hasAnnotation(checker.REP) ? "yes " : " no")
                            + (TreeUtils.isSelfAccess(e) ? " yes" : " no")
                            + (receiverImmutable ? " yes" : " no"));
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
        if (!TreeUtils.isSelfAccess(node.getVariable())
                && atypeFactory.isThis(node.getExpression())) {
            checker.report(Result.failure("anonymous.assigns.this.to.foreign.field"), node);
        }
    }

    /**
     * Check a function call inside an @Anonymous method.
     * @param node
     */
    protected void checkCallAnonymous(MethodInvocationTree node) {
        if (!TreeUtils.isSelfAccess(node)) {
            for (ExpressionTree arg : node.getArguments()) {
                if (atypeFactory.isThis(arg)) {
                    checker.report(Result.failure("anonymous.pass.this.to.foreign.method"), arg);
                } else {
                    System.err.println(arg.toString() + " is not this in call " + node.toString());
                }
            }
        } else {
            AnnotatedExecutableType method = atypeFactory.methodFromUse(node);
            if (!method.hasAnnotation(checker.ANONYMOUS)
                    && !isBaseConstructorCall(node)) {
                checker.report(Result.failure("anonymous.calls.non.anonymous"), node);
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
        System.err.println("Base? " + methodElement.getEnclosingElement().getSimpleName().toString()
                + " " + methodElement.getSimpleName());
        if (methodElement.getEnclosingElement().getSimpleName().contentEquals("Object")
                && methodElement.getSimpleName().contentEquals("<init>")) {
            return true;
        }
        return false;
    }
}
