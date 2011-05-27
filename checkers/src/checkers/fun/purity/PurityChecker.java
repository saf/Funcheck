/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.fun.purity;

import checkers.basetype.BaseTypeChecker;
import checkers.basetype.BaseTypeVisitor;
import checkers.fun.quals.*;
import checkers.quals.TypeQualifiers;
import checkers.types.AnnotatedTypeFactory;
import checkers.util.AnnotationUtils;
import checkers.util.GraphQualifierHierarchy;
import com.sun.source.tree.CompilationUnitTree;
import java.io.IOException;
import java.util.Collection;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;

/**
 *
 * @author saf
 */
@TypeQualifiers({
    /* Class qualifiers */
    PureClass.class, 
    /* Method qualifiers */
    Pure.class, 
    /* Type qualifiers */
    Local.class, Fresh.class
})
public class PurityChecker extends BaseTypeChecker {

    protected AnnotationUtils annotationFactory;

    public AnnotationMirror FRESH, LOCAL, PURE;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        annotationFactory = AnnotationUtils.getInstance(processingEnv);
        FRESH = annotationFactory.fromClass(Fresh.class);
        super.init(processingEnv);
    }

    @Override
    public AnnotatedTypeFactory createFactory(CompilationUnitTree root) {
        return new PurityAnnotatedTypeFactory(this, root);
    }

    @Override
    protected BaseTypeVisitor<?, ?> createSourceVisitor(CompilationUnitTree root) {
        try {
            return new PurityVisitor(this, root);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AnnotationUtils getAnnotationFactory() {
        return annotationFactory;
    }

}

