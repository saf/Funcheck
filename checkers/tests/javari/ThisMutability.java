import checkers.javari.quals.*;
import java.util.Date;

/**
 * This is to test that THIS-MUTABLE resolves properly!
 */
public class ThisMutability {
    @Assignable /*this-mutable*/ Date tm;

    void testMutable() @Mutable {
        @ReadOnly Date ro = null;
        @Mutable Date m = null;

        ro = tm;
        m = tm;

        //:: (assignment.type.incompatible)
        tm = ro;    // error
        tm = m;
    }

    void testReadOnly() @ReadOnly {
        @ReadOnly Date ro = null;
        @Mutable Date m = null;


        ro = tm;
        //:: (assignment.type.incompatible)
        m = tm;     // error

        //:: (assignment.type.incompatible)
        tm = ro;    // error
        tm = m;
    }

    void testPolyRead() @PolyRead {
        @ReadOnly Date ro = null;
        @Mutable Date m = null;

        ro = tm;
        //:: (assignment.type.incompatible)
        m = tm;     // error

        //:: (assignment.type.incompatible)
        tm = ro;    // error
        tm = m;
    }
}
