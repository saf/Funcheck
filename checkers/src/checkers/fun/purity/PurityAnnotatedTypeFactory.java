/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.fun.purity;

import checkers.basetype.BaseTypeChecker;
import checkers.types.BasicAnnotatedTypeFactory;
import checkers.util.AnnotationUtils;
import com.sun.source.tree.CompilationUnitTree;

/**
 *
 * @author saf
 */
public class PurityAnnotatedTypeFactory extends BasicAnnotatedTypeFactory<PurityChecker> {

    protected final AnnotationUtils annotationFactory;
    protected PurityChecker checker;

    public PurityAnnotatedTypeFactory(PurityChecker checker, CompilationUnitTree root) {
        super(checker, root, true);
        annotationFactory = checker.getAnnotationFactory();
        this.checker = checker;
    }

}
