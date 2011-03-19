package checkers.fun.jimuva;

import checkers.basetype.BaseTypeChecker;
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
import checkers.util.AnnotationUtils;
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

    AnnotationUtils annotationFactory;

    public AnnotationMirror IMMUTABLE, MUTABLE, READONLY, REP;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        annotationFactory = AnnotationUtils.getInstance(processingEnv);
        IMMUTABLE = annotationFactory.fromClass(Immutable.class);
        MUTABLE   = annotationFactory.fromClass(Mutable.class);
        READONLY  = annotationFactory.fromClass(ReadOnly.class);
        REP       = annotationFactory.fromClass(Rep.class);
        super.init(processingEnv);
    }

    public AnnotationUtils getAnnotationFactory() {
        return annotationFactory;
    }
}
