package checkers.fun.jimuva;

import checkers.fun.quals.Anonymous;
import checkers.fun.quals.Immutable;
import checkers.fun.quals.ReadOnly;
import checkers.fun.quals.WriteLocal;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedArrayType;
import checkers.types.AnnotatedTypeMirror.AnnotatedExecutableType;
import checkers.types.BasicAnnotatedTypeFactory;
import checkers.util.AnnotationUtils;
import checkers.util.ElementUtils;
import checkers.util.TreeUtils;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

/**
 * Determines annotations based on rules governing Funcheck's annotations.
 */
public class JimuvaAnnotatedTypeFactory extends BasicAnnotatedTypeFactory<JimuvaChecker> {

    protected final AnnotationUtils annotationFactory;
    protected JimuvaChecker checker;

    public JimuvaAnnotatedTypeFactory(JimuvaChecker checker, CompilationUnitTree root) {
        super(checker, root, false);
        annotationFactory = checker.getAnnotationFactory();
        this.checker = checker;
        // this.defaults = new JimuvaQualifierDefaults(this, this.annotations, checker);
    }

    @Override
    protected void annotateImplicit(Tree tree, AnnotatedTypeMirror type) {
        //annotateExpressionWithImmutability(tree, type);
        super.annotateImplicit(tree, type);
        addMutableAnnotation(type);
    }

    /**
     * Annotate the receivers of @ReadOnly methods with @Immutable
     * in order to permit calls of form
     *   imo.foo()
     * where imo is @Immutable and foo is @ReadOnly.
     *
     * @param tree the method call AST tree node
     * @return the annotated type for method called at [tree]
     */
    @Override
    public AnnotatedExecutableType methodFromUse(MethodInvocationTree tree) {
        AnnotatedExecutableType method = super.methodFromUse(tree);
        refineMethodType(method);
        addMutableAnnotation(method);
        return method;
    }

    @Override
    public AnnotatedTypeMirror fromElement(Element elt) {
        AnnotatedTypeMirror type = super.fromElement(elt);
        if (elt.getKind().equals(ElementKind.METHOD)) {
            refineMethodType((AnnotatedExecutableType) type);
        }
        return type;
    }

    @Override
    public AnnotatedTypeMirror getAnnotatedType(Tree tree) {
        AnnotatedTypeMirror type = super.getAnnotatedType(tree);
        if (tree.getKind() == Tree.Kind.METHOD) {
            refineMethodType((AnnotatedExecutableType) type);
        } else if (!type.hasAnnotation(checker.IMMUTABLE)) {
            type.addAnnotation(checker.MUTABLE);
        }
        return type;
    }

    @Override
    public AnnotatedTypeMirror getAnnotatedType(Element elt) {
        AnnotatedTypeMirror type = super.getAnnotatedType(elt);
        if (!type.hasAnnotation(checker.IMMUTABLE)) {
            type.addAnnotation(checker.MUTABLE);
        }
        return type;
    }

    protected void refineMethodType(AnnotatedExecutableType type) {
        if (type.hasAnnotation(checker.READONLY)) {
            AnnotatedTypeMirror.AnnotatedDeclaredType receiver = type.getReceiverType();
            receiver.removeAnnotation(checker.MUTABLE);
            receiver.addAnnotation(checker.IMMUTABLE);
        }
    }

    @Deprecated
    protected void annotateExpressionWithImmutability(Tree tree, AnnotatedTypeMirror type) {
        if (tree instanceof LiteralTree) {
            type.addAnnotation(checker.IMMUTABLE);
        } else if (tree instanceof BinaryTree) {
            BinaryTree bt = (BinaryTree) tree;
            AnnotatedTypeMirror left = getAnnotatedType(bt.getLeftOperand());
            AnnotatedTypeMirror right = getAnnotatedType(bt.getRightOperand());
            if (left.hasAnnotation(checker.IMMUTABLE) && right.hasAnnotation(checker.IMMUTABLE)) {
                type.addAnnotation(checker.IMMUTABLE);
            }
        } else if (tree instanceof ConditionalExpressionTree) {
            ConditionalExpressionTree ct = (ConditionalExpressionTree) tree;
            AnnotatedTypeMirror tval = getAnnotatedType(ct.getTrueExpression());
            AnnotatedTypeMirror fval = getAnnotatedType(ct.getTrueExpression());
            if (tval.hasAnnotation(checker.IMMUTABLE) && fval.hasAnnotation(checker.IMMUTABLE)) {
                type.addAnnotation(checker.IMMUTABLE);
            }
        } else if (tree instanceof IdentifierTree) {
            IdentifierTree it = (IdentifierTree) tree;
            if (getAnnotatedType(TreeUtils.elementFromUse(it)).getKind().isPrimitive()) {
                type.removeAnnotation(checker.IMMUTABLE);
                type.addAnnotation(checker.MUTABLE);
            }
        } else if (tree instanceof NewClassTree) {
            /*
             * A new object is always born to be immutable. The type of the variable
             * it is referenced by determines whether it becomes mutable afterwards.
             */
            type.addAnnotation(checker.IMMUTABLE);
        }
    }

    protected void addMutableAnnotation(AnnotatedTypeMirror type) {
        if (!type.hasAnnotation(checker.IMMUTABLE)) {
            type.addAnnotation(checker.MUTABLE);
        };
        
        if (type instanceof AnnotatedExecutableType) {
            AnnotatedExecutableType ext = (AnnotatedExecutableType) type;
            addMutableAnnotation(ext.getReceiverType());
            addMutableAnnotation(ext.getReturnType());
            for (AnnotatedTypeMirror t : ext.getParameterTypes()) {
                addMutableAnnotation(t);
            }
        } else if (type instanceof AnnotatedArrayType) {
            AnnotatedArrayType art = (AnnotatedArrayType) type;
            addMutableAnnotation(art.getComponentType());
        }
    }

    @Override
    protected void annotateImplicit(Element el, AnnotatedTypeMirror type) {
        super.annotateImplicit(el, type);

        addMutableAnnotation(type);

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
