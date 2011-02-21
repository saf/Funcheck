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

    protected NodeKey elk;

    public FuncChecker() {
        elk = new NodeKey();
    }

    /**
     * Fires the Prolog validator after completing the
     * AST traversal.
     */
    @Override
    public void typeProcessingOver() {
        Verifier v = new Verifier(this);
        v.run();
        super.typeProcessingOver();
    }

    public NodeKey getNodeMapping() {
        return elk;
    }
}
