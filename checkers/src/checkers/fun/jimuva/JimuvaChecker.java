package checkers.fun.jimuva;

import checkers.basetype.BaseTypeChecker;
import checkers.basetype.BaseTypeVisitor;
import checkers.fun.quals.*;
import checkers.quals.TypeQualifiers;
import checkers.types.AnnotatedTypeFactory;
import checkers.types.AnnotatedTypeMirror;
import checkers.types.AnnotatedTypeMirror.AnnotatedNullType;
import checkers.types.QualifierHierarchy;
import checkers.types.TypeHierarchy;
import checkers.util.AnnotationUtils;
import checkers.util.GraphQualifierHierarchy;
import com.sun.source.tree.CompilationUnitTree;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.tools.Diagnostic.Kind;

/**
 *
 * @author saf
 */
@TypeQualifiers({
    /* Class qualifiers */
    ImmutableClass.class, MutableClass.class,
    /* Object qualifiers */ 
    Mutable.class, Immutable.class,
    /* Method & constructor qualifiers */
    ReadOnly.class, ReadWrite.class, WriteLocal.class, Anonymous.class,
    /* Field qualifiers */
    Rep.class, Peer.class, World.class,
    /* Annotation for tracking references to this */
    This.class, NotThis.class, MaybeThis.class
})
public class JimuvaChecker extends BaseTypeChecker {

    protected AnnotationUtils annotationFactory;
    protected JimuvaVisitorState state;

    public AnnotationMirror IMMUTABLE, MUTABLE, READONLY, REP, PEER, WORLD, 
            ANONYMOUS, IMMUTABLE_CLASS,
            THIS, NOT_THIS, MAYBE_THIS;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        annotationFactory = AnnotationUtils.getInstance(processingEnv);
        IMMUTABLE  = annotationFactory.fromClass(Immutable.class);
        MUTABLE    = annotationFactory.fromClass(Mutable.class);
        READONLY   = annotationFactory.fromClass(ReadOnly.class);
        REP        = annotationFactory.fromClass(Rep.class);
        PEER       = annotationFactory.fromClass(Peer.class);
        WORLD      = annotationFactory.fromClass(World.class);
        ANONYMOUS  = annotationFactory.fromClass(Anonymous.class);
        IMMUTABLE_CLASS = annotationFactory.fromClass(ImmutableClass.class);
        THIS       = annotationFactory.fromClass(This.class);
        NOT_THIS   = annotationFactory.fromClass(NotThis.class);
        MAYBE_THIS = annotationFactory.fromClass(MaybeThis.class);
        state = new JimuvaVisitorState();
        super.init(processingEnv);
    }

    @Override
    public AnnotatedTypeFactory createFactory(CompilationUnitTree root) {
        return new JimuvaAnnotatedTypeFactory(this, root, state);
    }

    @Override
    protected BaseTypeVisitor<?, ?> createSourceVisitor(CompilationUnitTree root) {
        try {
            return new JimuvaVisitor(this, root, state);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AnnotationUtils getAnnotationFactory() {
        return annotationFactory;
    }

    /**
     * Issue a note to the error stream.
     *
     * @param messageKey
     * @param args
     */
    public void note(Object source, String messageKey, Object... args) {
        String msg = messageKey;
        if (messages.containsKey(messageKey)) {
            msg = messages.getProperty(messageKey);
        }
        if (source != null) {
            message(Kind.OTHER, source, String.format(msg, args));
        } else {
            message(Kind.NOTE, null, String.format(msg, args));
        }
    }

    public static class JimuvaQualifierHierarchy extends GraphQualifierHierarchy {

        public JimuvaQualifierHierarchy(GraphQualifierHierarchy h) {
            super(h);
        }

        /**
         * Every annotation from LHS must have a match in RHS.
         *
         * This is because different sets of annotations cover various
         * features of an element.
         *
         * @param rhs
         * @param lhs
         * @return
         */
        @Override
        public boolean isSubtype(Collection<AnnotationMirror> rhs, Collection<AnnotationMirror> lhs) {
            Collection<AnnotationMirror> unmatched = new HashSet<AnnotationMirror>(lhs);

            for (AnnotationMirror al : lhs) {
                if (!AnnotationUtils.isTypeAnnotation(al)) {
                    unmatched.remove(al);
                } else {
                    for (AnnotationMirror ar : rhs) {
                        if (isSubtype(ar, al)) {
                            unmatched.remove(al);
                        }
                    }
                }
            }
            return unmatched.isEmpty();
        }
    }

    /**
     * An AnnotatedNullTypeMirror may be a subtype of anything.
     * @param rhs the potential subtype
     * @param lhs the supertype
     * @return
     */
    @Override
    public boolean isSubtype(AnnotatedTypeMirror rhs, AnnotatedTypeMirror lhs) {
            if (rhs instanceof AnnotatedNullType) {
                return true;
            } else {
                return super.isSubtype(rhs, lhs);
            }
    }

    @Override
    protected QualifierHierarchy createQualifierHierarchy() {
        QualifierHierarchy hierarchy = super.createQualifierHierarchy();
        return new JimuvaQualifierHierarchy((GraphQualifierHierarchy) hierarchy);
    }


}
