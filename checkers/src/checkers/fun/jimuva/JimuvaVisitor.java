package checkers.fun.jimuva;

import checkers.basetype.BaseTypeVisitor;
import checkers.fun.quals.Function;
import checkers.source.Result;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedDeclaredType;
import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import java.io.IOException;
import javax.lang.model.element.AnnotationMirror;

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
    protected void commonAssignmentCheck(AnnotatedTypeMirror varType, AnnotatedTypeMirror valueType, Tree valueTree, String errorKey, Void p) {
        if (varType.hasAnnotation(checker.MUTABLE)
                && valueType.hasAnnotation(checker.IMMUTABLE)) {
            checker.report(Result.failure("immut.assignment.would.violate"), valueTree);
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
        return super.visitMethod(node, p);
    }

    @Override
    public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
        AnnotatedExecutableType calledMethod = atypeFactory.methodFromUse(node);
        for (AnnotationMirror mi : calledMethod.getAnnotations()) {
            System.err.println("Annotation for " + calledMethod.getElement().getSimpleName() + ": " + mi.toString());
        }
        return super.visitMethodInvocation(node, p);
    }
}
