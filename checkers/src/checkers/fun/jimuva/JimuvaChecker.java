package checkers.fun.jimuva;

import checkers.basetype.BaseTypeChecker;
import checkers.basetype.BaseTypeVisitor;
import checkers.fun.quals.Anonymous;
import checkers.fun.quals.Immutable;
import checkers.fun.quals.ImmutableClass;
import checkers.fun.quals.Mutable;
import checkers.fun.quals.MutableClass;
import checkers.fun.quals.ReadOnly;
import checkers.fun.quals.ReadWrite;
import checkers.fun.quals.Rep;
import checkers.fun.quals.WriteLocal;
import checkers.quals.TypeQualifiers;
import checkers.types.AnnotatedTypeFactory;
import checkers.util.AnnotationUtils;
import com.sun.source.tree.CompilationUnitTree;
import java.io.IOException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

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
    Rep.class
})
public class JimuvaChecker extends BaseTypeChecker {

    protected AnnotationUtils annotationFactory;
    protected JimuvaVisitorState state;

    public AnnotationMirror IMMUTABLE, MUTABLE, READONLY, REP, 
            ANONYMOUS, IMMUTABLE_CLASS;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        annotationFactory = AnnotationUtils.getInstance(processingEnv);
        IMMUTABLE  = annotationFactory.fromClass(Immutable.class);
        MUTABLE    = annotationFactory.fromClass(Mutable.class);
        READONLY   = annotationFactory.fromClass(ReadOnly.class);
        REP        = annotationFactory.fromClass(Rep.class);
        ANONYMOUS  = annotationFactory.fromClass(Anonymous.class);
        IMMUTABLE_CLASS = annotationFactory.fromClass(ImmutableClass.class);
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
}
