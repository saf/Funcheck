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
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

/**
 *
 * @author saf
 */
public class JimuvaVisitor extends BaseTypeVisitor<Void, Void> {

    protected JimuvaChecker checker;

    protected AnnotatedExecutableType currentMethod;
    protected AnnotatedDeclaredType currentClass;

    public JimuvaVisitor(JimuvaChecker checker, CompilationUnitTree root) throws IOException {
        super(checker, root);
        this.checker = checker;
    }

    @Override
    public Void visitAssignment(AssignmentTree node, Void p) {
        System.err.println(node.toString());
        ExpressionTree varTree = node.getVariable();
        ExpressionTree valueTree = node.getExpression();

        AnnotatedTypeMirror varType = atypeFactory.getAnnotatedType(varTree);
        AnnotatedTypeMirror valueType = atypeFactory.getAnnotatedType(valueTree);

        AnnotatedTypeMirror receiver = atypeFactory.getReceiver(varTree);

//        if (currentMethod.hasAnnotation(checker.READONLY)) {
//
//            System.err.println(receiver == null ? "null" : receiver.toString());
//
//            if (TreeUtils.isSelfAccess(varTree)) {
//                checker.report(Result.failure("readonly.assigns.field"), varTree);
//            }
//        }

        if (receiver != null && receiver.hasAnnotation(checker.IMMUTABLE)) {
            checker.report(Result.failure("assigning.field.of.immutable"), varTree);
        }

        return super.visitAssignment(node, p);
    }



    @Override
    protected void commonAssignmentCheck(AnnotatedTypeMirror varType, AnnotatedTypeMirror valueType, Tree valueTree, String errorKey, Void p) {
        if (varType.hasAnnotation(checker.MUTABLE)
                && valueType.hasAnnotation(checker.IMMUTABLE)) {
            checker.report(Result.failure(errorKey, valueType.toString(), varType.toString()), valueTree);
        } else if (varType.hasAnnotation(checker.IMMUTABLE)
                && valueType.hasAnnotation(checker.MUTABLE)) {
            /* @Immutable <-- @Mutable assignment is OK */
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
        System.err.println("current method is: " + node.getName().toString() + "(" + currentMethod + ")");
        return super.visitMethod(node, p);
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
        AnnotatedExecutableType calledMethod = atypeFactory.methodFromUse(node);
        System.err.println(node.toString());
        AnnotatedTypeMirror receiver = atypeFactory.getReceiver(node);

//        if ((receiver != null && receiver.hasAnnotation(checker.IMMUTABLE))
//                && !calledMethod.hasAnnotation(checker.READONLY)) {
//            checker.report(Result.failure("immut.unsafe.call"), node);
//        };

//        if (currentMethod.hasAnnotation(checker.READONLY)) {
//            if (TreeUtils.isSelfAccess(node)
//                    && !calledMethod.hasAnnotation(checker.READONLY)) {
//                checker.report(Result.failure("immut.readonly.calls.readwrite"), node);
//            } else if (receiver != null) {
//                Element receiverElement = receiver.getElement();
//                if (receiverElement.getKind().equals(ElementKind.FIELD)
//                        && receiver.hasAnnotation(checker.REP)) {
//                    checker.report(Result.failure("immut.readonly.calls.readwrite.on.representation"), node);
//                }
//            }
//        }

        for (AnnotationMirror mi : calledMethod.getAnnotations()) {
            System.err.println("Annotation for " + calledMethod.getElement().getSimpleName() + ": " + mi.toString());
        }
        return super.visitMethodInvocation(node, p);
    }

    @Override
    protected boolean checkMethodInvocability(AnnotatedExecutableType method, MethodInvocationTree node) {
        AnnotatedTypeMirror methodReceiver = method.getReceiverType().getErased();
        AnnotatedTypeMirror treeReceiver = methodReceiver.getCopy(false);
        treeReceiver.addAnnotations(atypeFactory.getReceiver(node).getAnnotations());

        /* Allow for @ReadOnly calls on @Mutable references. */
        if (treeReceiver.hasAnnotation(checker.MUTABLE)
                && methodReceiver.hasAnnotation(checker.IMMUTABLE)) {
            treeReceiver.removeAnnotation(checker.MUTABLE);
            methodReceiver.removeAnnotation(checker.IMMUTABLE);
        }
        /* Otherwise, act like the BaseTypeChecker. */
        if (!checker.isSubtype(treeReceiver, methodReceiver)) {
            checker.report(Result.failure("method.invocation.invalid",
                    TreeUtils.elementFromUse(node),
                    treeReceiver.toString(), methodReceiver.toString()), node);
            return false;
        }
        return true;
    }




}
