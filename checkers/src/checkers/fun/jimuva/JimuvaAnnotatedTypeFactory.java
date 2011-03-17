package checkers.fun.jimuva;

import checkers.fun.quals.Anonymous;
import checkers.fun.quals.Immutable;
import checkers.fun.quals.ReadOnly;
import checkers.fun.quals.WriteLocal;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.BasicAnnotatedTypeFactory;
import checkers.util.AnnotationUtils;
import checkers.util.ElementUtils;
import checkers.util.TreeUtils;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

/**
 * Determines annotations based on rules governing Funcheck's annotations.
 */
public class JimuvaAnnotatedTypeFactory extends BasicAnnotatedTypeFactory<JimuvaChecker> {

    protected final AnnotationUtils annotationFactory;

    protected final AnnotationMirror IMMUTABLE, MUTABLE;

    public JimuvaAnnotatedTypeFactory(JimuvaChecker checker, CompilationUnitTree root) {
        super(checker, root, false);
        annotationFactory = checker.getAnnotationFactory();
        IMMUTABLE = checker.IMMUTABLE;
        MUTABLE   = checker.MUTABLE;
    }

    @Override
    protected void annotateImplicit(Tree tree, AnnotatedTypeMirror type) {
        //annotateExpressionWithImmutability(tree, type);
        super.annotateImplicit(tree, type);
    }

    protected void annotateExpressionWithImmutability(Tree tree, AnnotatedTypeMirror type) {
        if (tree instanceof LiteralTree) {
            type.addAnnotation(IMMUTABLE);
        } else if (tree instanceof BinaryTree) {
            BinaryTree bt = (BinaryTree) tree;
            AnnotatedTypeMirror left = getAnnotatedType(bt.getLeftOperand());
            AnnotatedTypeMirror right = getAnnotatedType(bt.getRightOperand());
            if (left.hasAnnotation(IMMUTABLE) && right.hasAnnotation(IMMUTABLE)) {
                type.addAnnotation(IMMUTABLE);
            }
        } else if (tree instanceof ConditionalExpressionTree) {
            ConditionalExpressionTree ct = (ConditionalExpressionTree) tree;
            AnnotatedTypeMirror tval = getAnnotatedType(ct.getTrueExpression());
            AnnotatedTypeMirror fval = getAnnotatedType(ct.getTrueExpression());
            if (tval.hasAnnotation(IMMUTABLE) && fval.hasAnnotation(IMMUTABLE)) {
                type.addAnnotation(IMMUTABLE);
            }
        } else if (tree instanceof IdentifierTree) {
            IdentifierTree it = (IdentifierTree) tree;
            if (getAnnotatedType(TreeUtils.elementFromUse(it)).getKind().isPrimitive()) {
                type.removeAnnotation(IMMUTABLE);
                type.addAnnotation(MUTABLE);
            }
        } else if (tree instanceof NewClassTree) {
            /*
             * A new object is always born to be immutable. The type of the variable
             * it is referenced by determines whether it becomes mutable afterwards.
             */
            type.addAnnotation(IMMUTABLE);
        }
    }

    @Override
    protected void annotateImplicit(Element el, AnnotatedTypeMirror type) {
        super.annotateImplicit(el, type);

        /* @Immutable classes may only have @ReadOnly methods and @Anonymous @WriteLocal constructors */
        if (el.getKind() == ElementKind.METHOD) {
            TypeElement classElement = ElementUtils.enclosingClass(el);
            AnnotatedTypeMirror.AnnotatedDeclaredType annotClass = getAnnotatedType(classElement);
            if (annotClass.hasAnnotation(Immutable.class)) {
                System.err.println("Annotating " + el.getSimpleName() + " with ReadOnly");
                type.addAnnotation(ReadOnly.class);
            }
        }

        if (el.getKind() == ElementKind.CONSTRUCTOR) {
            TypeElement classElement = ElementUtils.enclosingClass(el);
            AnnotatedTypeMirror.AnnotatedDeclaredType annotClass = getAnnotatedType(classElement);
            if (annotClass.hasAnnotation(Immutable.class)) {
                System.err.println("Annotating " + el.getSimpleName() + " with Anonymous");
                type.addAnnotation(Anonymous.class);
                type.addAnnotation(WriteLocal.class);
            }
        }
    }
}
