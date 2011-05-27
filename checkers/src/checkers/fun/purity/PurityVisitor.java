/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.fun.purity;

import checkers.basetype.BaseTypeVisitor;
import com.sun.source.tree.CompilationUnitTree;
import java.io.IOException;

/**
 *
 * @author saf
 */
public class PurityVisitor extends BaseTypeVisitor<Void, Void> {

    protected PurityChecker checker;

    protected PurityAnnotatedTypeFactory atypeFactory;

    public PurityVisitor(PurityChecker checker, CompilationUnitTree root) throws IOException {
        super(checker, root);
        this.checker = checker;
        atypeFactory = new PurityAnnotatedTypeFactory(checker, root);
    }

}
