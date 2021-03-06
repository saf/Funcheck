import checkers.nullness.quals.*;

public class Casts {

    void test(String nonNullParam) {
        Object lc1 = (Object) nonNullParam;
        lc1.toString();

        String nullable = null;
        Object lc2 = (Object) nullable;
        //:: (dereference.of.nullable)
        lc2.toString(); // error
    }

    void testBoxing() {
        Integer b = null;
        //:: (assignment.type.incompatible)
        int i = b;
        //:: (unboxing.of.nullable)
        Object o = (int)b;
    }
}
