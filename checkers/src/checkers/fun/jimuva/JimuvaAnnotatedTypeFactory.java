package checkers.fun.jimuva;

import checkers.flow.Flow;
import checkers.flow.GenKillBits;
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
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.Tree;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Determines annotations based on rules governing Funcheck's annotations.
 */
public class JimuvaAnnotatedTypeFactory extends BasicAnnotatedTypeFactory<JimuvaChecker> {

    protected final AnnotationUtils annotationFactory;
    protected JimuvaChecker checker;
    protected JimuvaVisitorState state;

    public JimuvaAnnotatedTypeFactory(JimuvaChecker checker, CompilationUnitTree root, JimuvaVisitorState state) {
        super(checker, root, true);
        annotationFactory = checker.getAnnotationFactory();
        this.checker = checker;
        this.state = state;
        state.setFactory(this);
    }

    @Override
    public AnnotatedTypeMirror getAnnotatedType(Tree tree) {
        AnnotatedTypeMirror type = super.getAnnotatedType(tree);

        if (tree.getKind() == Tree.Kind.METHOD_INVOCATION) {
            MethodInvocationTree inv = (MethodInvocationTree) tree;
            ExecutableElement mElem = TreeUtils.elementFromUse(inv);
            AnnotatedExecutableType mType = getAnnotatedType(mElem);
            AnnotatedTypeMirror receiverType = getReceiver(inv);
            if (receiverType != null &&
                    (receiverType.hasAnnotation(checker.THIS) 
                    || receiverType.hasAnnotation(checker.MAYBE_THIS)
                    || TreeUtils.isSelfAccess(inv))) {
                if (mType.hasAnnotation(checker.ANONYMOUS)) {
                    type.addAnnotation(checker.NOT_THIS);
                } else {
                    type.addAnnotation(checker.MAYBE_THIS);
                }
            } else {
                /* #TODO Is this valid? */
                type.addAnnotation(checker.NOT_THIS);
            }
        } else if (tree.getKind() == Tree.Kind.IDENTIFIER) {
            IdentifierTree ident = (IdentifierTree) tree;
            if (ident.getName().contentEquals("this")) {
                type.addAnnotation(checker.THIS);
            }
        } else if (tree.getKind() == Tree.Kind.NEW_ARRAY
                || tree.getKind() == Tree.Kind.NEW_CLASS) {
            type.addAnnotation(checker.NOT_THIS);
        }
        return type;
    }

    @Override
    public AnnotatedTypeMirror getAnnotatedType(Element elt) {
        AnnotatedTypeMirror type = super.getAnnotatedType(elt);
        if (elt.getKind() == ElementKind.PARAMETER) {
            type.addAnnotation(checker.NOT_THIS);
        }
        return type;
    }



    @Override
    protected void annotateImplicit(Tree tree, AnnotatedTypeMirror type) {
        //annotateExpressionWithImmutability(tree, type);
        super.annotateImplicit(tree, type);
        if (tree.getKind() == Tree.Kind.METHOD) {
            refineMethodType((AnnotatedExecutableType) type);
        }
        addMutableAnnotation(type);
    }

    @Override
    protected void annotateImplicit(Element el, AnnotatedTypeMirror type) {
        super.annotateImplicit(el, type);
        if (el.getKind() == ElementKind.CONSTRUCTOR || el.getKind() == ElementKind.METHOD) {
            refineMethodType((AnnotatedExecutableType) type);
        }
        addMutableAnnotation(type);
    }

    protected void refineMethodType(AnnotatedExecutableType type) {
        /* Add implicit annotations to methods of Immutable classes */
        ExecutableElement el = type.getElement();
        TypeElement enclosingClass = ElementUtils.enclosingClass(el);
        AnnotatedTypeMirror.AnnotatedDeclaredType enclosingClassType =
                getAnnotatedType(enclosingClass);
        if (enclosingClassType.hasAnnotation(checker.IMMUTABLE_CLASS)) {
            AnnotationMirror implicit = el.getKind() == ElementKind.CONSTRUCTOR
                    ? checker.ANONYMOUS
                    : checker.READONLY;
            if (!type.hasAnnotation(implicit)) {
                type.addAnnotation(implicit);
                state.addImplicitAnnotation(el, implicit);
            }
        }

        /* Add the @Immutable annotation to receivers of @ReadOnly methods */
        if (type.hasAnnotation(checker.READONLY)) {
            AnnotatedTypeMirror.AnnotatedDeclaredType receiver = type.getReceiverType();
            if (receiver != null) {
                receiver.removeAnnotation(checker.MUTABLE);
                receiver.addAnnotation(checker.IMMUTABLE);
            }
        }
    }

    /**
     * Add the implicit @Mutable annotation to avoid unqualified types.
     *
     * @param type the type to be refined.
     */
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

    public static class JimuvaFlow extends Flow {
        protected JimuvaChecker checker;

        public JimuvaFlow(JimuvaChecker checker, CompilationUnitTree root,
                Set<AnnotationMirror> flowQuals, JimuvaAnnotatedTypeFactory factory) {
                super(checker, root, flowQuals, factory);
                this.checker = checker;
        }

        @Override
        protected void merge(GenKillBits<AnnotationMirror> bits, GenKillBits<AnnotationMirror> other) {
            bits.or(other);
            for (int i = 0; i < vars.size(); i++) {
                if (bits.get(checker.MAYBE_THIS, i)
                        || (bits.get(checker.THIS, i) && bits.get(checker.NOT_THIS, i))) {
                    bits.clear(checker.THIS, i);
                    bits.clear(checker.NOT_THIS, i);
                    bits.set(checker.MAYBE_THIS, i);
                }
            }
        }
    }

    @Override
    protected Flow createFlow(JimuvaChecker checker, CompilationUnitTree root, Set<AnnotationMirror> flowQuals) {
        return new JimuvaFlow(checker, root, flowQuals, this);
    }

    @Override
    protected Set<AnnotationMirror> createFlowQualifiers(JimuvaChecker checker) {
        Set<AnnotationMirror> flowQuals = new HashSet<AnnotationMirror>();
        flowQuals.add(checker.THIS);
        flowQuals.add(checker.NOT_THIS);
        flowQuals.add(checker.MAYBE_THIS);
        return flowQuals;
    }


}
