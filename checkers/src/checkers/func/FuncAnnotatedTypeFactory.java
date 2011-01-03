package checkers.func;

import checkers.types.BasicAnnotatedTypeFactory;
import com.sun.source.tree.CompilationUnitTree;

/**
 * Determines annotations based on rules governing Funcheck's annotations.
 */
public class FuncAnnotatedTypeFactory extends BasicAnnotatedTypeFactory<FuncChecker> {
    public FuncAnnotatedTypeFactory(FuncChecker checker, CompilationUnitTree root) {
        super(checker, root, false);
    }
}
