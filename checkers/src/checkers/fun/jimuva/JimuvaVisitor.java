package checkers.fun.jimuva;

import checkers.basetype.BaseTypeVisitor;
import checkers.fun.quals.Anonymous;
import checkers.fun.quals.Function;
import checkers.fun.quals.ImmutableClass;
import checkers.source.Result;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedDeclaredType;
import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;
import checkers.util.ElementUtils;
import checkers.util.InternalUtils;
import checkers.util.TreeUtils;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.tools.javac.util.Pair;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic.Kind;

/**
 *
 * @author saf
 */
public class JimuvaVisitor extends BaseTypeVisitor<Void, Void> {

    protected JimuvaChecker checker;

    protected JimuvaAnnotatedTypeFactory atypeFactory;
    protected JimuvaVisitorState state;

    public JimuvaVisitor(JimuvaChecker checker, CompilationUnitTree root, JimuvaVisitorState state) throws IOException {
        super(checker, root);
        this.checker = checker;
        atypeFactory = new JimuvaAnnotatedTypeFactory(checker, root, state);
        this.state = state;
    }

    @Override
    public Void visitAssignment(AssignmentTree node, Void p) {
        ExpressionTree varTree = node.getVariable();

        AnnotatedTypeMirror receiver = atypeFactory.getReceiver(varTree);
        //System.err.println("Receiver type is " + receiver.toString());
        if (receiver != null && receiver.hasAnnotation(checker.IMMUTABLE)) {
            if (TreeUtils.isSelfAccess(varTree)) {
                checker.report(Result.failure("assign.readonly.receiver.field", 
                        varTree.toString(), state.getCurrentMethodName()), node);
                if (state.inImplicitlyAnnotatedMethod()) {
                    checker.note(null, "readonly.implicit",
                            state.getCurrentMethodName(), state.getCurrentClassName());
                }
            } else {
                checker.report(Result.failure("assign.immutable.field"), node);
            }
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
                state.addThisAlias(varElement, node);
            } catch (IllegalArgumentException e) {
                /* The node was not an element use. Swallow the exception. */
            }
        }

        return super.visitAssignment(node, p);
    }

    @Override
    public Void visitVariable(VariableTree node, Void p) {
        /* Check that no @Rep field is public */
        VariableElement el = TreeUtils.elementFromDeclaration(node);
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(el);
        if (el.getKind().isField()
                && el.getModifiers().contains(Modifier.PUBLIC)
                && type.hasAnnotation(checker.REP)) {
            checker.report(Result.failure("public.rep.field", el.getSimpleName()), node);
        }
        return super.visitVariable(node, p);
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
        AnnotatedTypeMirror calledMethodReceiver = calledMethod.getReceiverType();

        /* Method calls cannot alter the inner representation of @Immutable objects */
        if (state.isCurrentMethod(checker.IMMUTABLE)
                && receiver != null && receiver.hasAnnotation(checker.REP)
                && calledMethodReceiver != null && !calledMethodReceiver.hasAnnotation(checker.IMMUTABLE)) {
            checker.report(Result.failure("nonreadonly.call.on.rep",
                    calledMethod.getElement().getSimpleName().toString(),
                    calledMethodReceiver.getElement().getSimpleName().toString()), node);
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
    public Void visitNewArray(NewArrayTree node, Void p) {
        checkNewReferenceType(node.getType());
        checkNewReferenceFromDefaultConstructor(node.getType());
        return super.visitNewArray(node, p);
    }

    /**
     * Verify that no immutable reference is created using a non-@Anonymous
     * constructor while creating a new array
     * 
     * @param tree The array type specification tree.
     */
    protected void checkNewReferenceFromDefaultConstructor(Tree tree) {
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(tree);
        if (type.getKind() == TypeKind.DECLARED) {
            Element element = ((AnnotatedDeclaredType) type).getUnderlyingType().asElement();
            assert element.getKind() == ElementKind.CLASS : "Array initializer not a class";
            TypeElement typeElement = (TypeElement) element;
            if (type.hasAnnotation(checker.IMMUTABLE) && !isDefaultConstructorAnonymous(typeElement)) {
                checker.report(Result.warning("immutable.untrusted.constructor"), tree);
            }
        } else if (type.getKind() == TypeKind.ARRAY) {
            ArrayTypeTree aTree = (ArrayTypeTree) tree;
            checkNewReferenceFromDefaultConstructor(aTree.getType());
        }
    }

    protected Boolean isDefaultConstructorAnonymous(TypeElement typeElement) {
        for (Element el : typeElement.getEnclosedElements()) {
            if (el.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructor = (ExecutableElement) el;
                if (constructor.getParameters().isEmpty()) {
                    return constructor.getAnnotation(Anonymous.class) != null;
                }
            }
        }
        TypeMirror superclass = typeElement.getSuperclass();
        if (superclass.getKind() == TypeKind.NONE) {
            /* The constructor is Object.<init>(). It does nothing, so we
               assume that it is anonymous. */
            return true;
        } else if (superclass.getKind() == TypeKind.DECLARED) {
            Element superclassElement = ((DeclaredType) superclass).asElement();
            if (superclassElement.getKind() == ElementKind.CLASS) {
                /* Proceed with the superclass */
                return isDefaultConstructorAnonymous((TypeElement) superclassElement);
            } else {
                throw new IllegalStateException("Superclass is not a class");
            }
        } else {
            throw new IllegalStateException("Superclass is not a DeclaredType or NoType");
        }
    }

    @Override
    public Void visitNewClass(NewClassTree node, Void p) {
        if (state.isCurrentMethod(checker.ANONYMOUS)) {
            /* Do not pass this to foreign constructors. */
            checkArgumentsAnonymous(node.getArguments());
        }
        checkNewReferenceType(node.getIdentifier());
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(node.getIdentifier());
        AnnotatedExecutableType constructor = atypeFactory.constructorFromUse(node);
        if (type.hasAnnotation(checker.IMMUTABLE) 
                && constructor.getAnnotation(Anonymous.class) == null) {
            checker.report(Result.warning("immutable.untrusted.constructor"), node);
        }
        return super.visitNewClass(node, p);
    }

    /**
     * Check that the reference type is correct, i.e. that there are no
     * mutable references to objects of an ImmutableClass created.
     *
     * @param tree The Tree representing the declared type of the new object,
     */
    protected void checkNewReferenceType(Tree tree) {
        AnnotatedTypeMirror type = atypeFactory.getAnnotatedType(tree);
        if (type.getKind() == TypeKind.DECLARED) {
            AnnotatedDeclaredType declaredType = (AnnotatedDeclaredType) type;
            Element typeElement = declaredType.getUnderlyingType().asElement();
            if (typeElement.getKind() == ElementKind.CLASS) {
                if (typeElement.getAnnotation(ImmutableClass.class) != null
                        && !type.hasAnnotation(checker.IMMUTABLE)) {
                    /* Creating a mutable reference to an immutable object */
                    checker.report(Result.failure("mutable.reference.to.immutable.class"), tree);
                }
            }
        } else if (type.getKind() == TypeKind.ARRAY) {
            ArrayTypeTree aTree = (ArrayTypeTree) tree;
            checkNewReferenceType(aTree.getType());
        }
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
//                System.err.println("ARRAY: " + e.toString() + " : " + et.toString());
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
            }
        }
    }

    /**
     * Check an assignment within an @Anonymous method.
     * @param node
     */
    protected void checkAssignmentAnonymous(AssignmentTree node) {
        ExpressionTree ex = node.getExpression();
        ExpressionTree var = node.getVariable();
        ThisReferenceSource thisSource = checkNotThis(ex);
        if (thisSource != null) {
            try {
                Element varElement = TreeUtils.elementFromUse(var);
                if (varElement.getKind() == ElementKind.FIELD) {
                    checker.report(Result.failure("anonymous.assigns.this.to.field",
                            var.toString()), var);
                    thisSource.print(checker);
                }
            } catch (IllegalArgumentException e) {
                /* Tree is not an element use */
                if (var.getKind() == Tree.Kind.ARRAY_ACCESS) {
                    checker.report(Result.failure("anonymous.assigns.this.to.array.field",
                            var.toString()), var);
                    thisSource.print(checker);
                }
            }
        }
    }

    /**
     * Check a function call inside an @Anonymous method.
     * @param node
     */
    protected void checkCallAnonymous(MethodInvocationTree node) {
        checkArgumentsAnonymous(node.getArguments());

        /* Check that non-@Anonymous methods are not called on [this] */
        AnnotatedExecutableType method = atypeFactory.methodFromUse(node);
        if (!method.hasAnnotation(checker.ANONYMOUS)
                    && !isBaseConstructorCall(node)) {
            if (TreeUtils.isSelfAccess(node)) {
                checker.report(Result.failure("anonymous.calls.non.anonymous", 
                        method.getElement().getSimpleName().toString(),
                        state.getCurrentMethodName()), node);
                if (state.inImplicitlyAnnotatedMethod()) {
                    checker.note(null, "anonymous.implicit", state.getCurrentMethodName());
                }
            } else {
                ExpressionTree select = node.getMethodSelect();
                if (select.getKind() == Tree.Kind.MEMBER_SELECT) {
                    MemberSelectTree selTree = (MemberSelectTree) select;
                    ThisReferenceSource selThis = checkNotThis(selTree);
                    if (selThis != null) {
                        checker.report(Result.failure("anonymous.calls.non.anonymous.on.alias"), node);
                        selThis.print(checker);
                        if (state.inImplicitlyAnnotatedMethod()) {
                            checker.note(null, "anonymous.implicit", state.getCurrentMethodName());
                        }
                    }
                }
            }
        }
    }

    /**
     * Validate arguments of a method/constructor call within an Anonymous
     * method by checking that they don't evaluate to a reference to this.
     *
     * @param args The list of arguments to be checked.
     */
    protected void checkArgumentsAnonymous(List<? extends ExpressionTree> args) {
        for (ExpressionTree arg : args) {
            ThisReferenceSource exThisSource = checkNotThis(arg);
            if (exThisSource != null) {
                checker.report(Result.failure("argument.may.be.this", arg.toString()), arg);
                exThisSource.print(checker);
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

    public enum ThisReferenceSource {
        THIS_LITERAL {
            public void print(JimuvaChecker ch) {}
        },
        ALIAS {
            public void print(JimuvaChecker ch) {
                ch.note(location, "this.ref.alias", aliasName);
            }
        },
        NONANONYMOUS_ON_THIS {
            public void print(JimuvaChecker ch) {
                ch.note(null, "this.ref.nonanonymous.on.this", methodName);
            }
        },
        NONANONYMOUS_ON_ALIAS {
            public void print(JimuvaChecker ch) {
                ch.note(null, "this.ref.nonanonymous.on.alias", methodName, aliasName);
                innerReferenceSource.print(ch);
            }
        };

        String aliasName;
        String methodName;
        ThisReferenceSource innerReferenceSource;
        Tree location;

        public void setLocation(Tree t) { this.location = t; }
        public void setAliasName(String s) { this.aliasName = s; }
        public void setMethodName(String s) { this.methodName = s; }
        public void setInnerReferenceSource(ThisReferenceSource innerReferenceSource) {
            this.innerReferenceSource = innerReferenceSource;
        }

        public abstract void print(JimuvaChecker ch);
    }

    /**
     * Return an error message key if the expression may evaluate to a reference to [this].
     * @param node the [ExpressionTree] to be checked.
     */
    protected ThisReferenceSource checkNotThis(ExpressionTree node) {
        ThisReferenceSource result = null;
        if (node.getKind() == Tree.Kind.IDENTIFIER) {
            IdentifierTree tree = (IdentifierTree) node;
            Element sym = InternalUtils.symbol(tree);
            if (state.isThisAlias(sym)) {
                result = ThisReferenceSource.ALIAS;
                result.setAliasName(sym.getSimpleName().toString());
                result.setLocation(state.getThisAssignment(sym));
                return result;
            } else if (tree.getName().contentEquals("this")) {
                return ThisReferenceSource.THIS_LITERAL;
            }
        } else if (node.getKind() == Tree.Kind.METHOD_INVOCATION) {
            MethodInvocationTree tree = (MethodInvocationTree) node;
            AnnotatedExecutableType method = atypeFactory.methodFromUse(tree);
            if (!method.hasAnnotation(checker.ANONYMOUS)) {
                if (TreeUtils.isSelfAccess(node)) {
                    result = ThisReferenceSource.NONANONYMOUS_ON_THIS;
                    result.setMethodName(method.getElement().getSimpleName().toString());
                    return result;
                } else {
                    ExpressionTree select = tree.getMethodSelect();
                    if (select.getKind() == Tree.Kind.MEMBER_SELECT) {
                        MemberSelectTree selTree = (MemberSelectTree) select;
                        ThisReferenceSource selError = checkNotThis(selTree.getExpression());
                        if (selError != null) {
                            result = ThisReferenceSource.NONANONYMOUS_ON_ALIAS;
                            result.setInnerReferenceSource(selError);
                            result.setMethodName(method.getElement().getSimpleName().toString());
                            result.setAliasName(selTree.getExpression().toString());
                        }
                    }
                }
            }
        }
        return result;
    }

    protected Boolean mayBeThis(ExpressionTree node) {
        return checkNotThis(node) != null;
    }
}