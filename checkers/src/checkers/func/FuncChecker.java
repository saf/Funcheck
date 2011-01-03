package checkers.func;

import checkers.basetype.BaseTypeChecker;
import checkers.func.quals.Function;
import checkers.func.quals.Immutable;
import checkers.quals.TypeQualifiers;

/**
 *
 * @author saf
 */
@TypeQualifiers({Function.class, Immutable.class})
public class FuncChecker extends BaseTypeChecker {

    

    /**
     * Fires the Prolog validator after completing the
     * AST traversal.
     */
    @Override
    public void typeProcessingOver() {
        
    }
}
