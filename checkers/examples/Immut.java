import checkers.fun.quals.*;

@ImmutableClass
public class Immut {

    static class IntHolder {
        public Integer i;

        public IntHolder() {
            i = 0;
        }

        void set(int j) { i = j; }
        Integer get() { return i; };
    }

    public static void main(String[] args) {
        @Immutable IntHolder h = new IntHolder();
        @Mutable IntHolder foo = h;
        foo.set(3);
        System.out.println(h.get().toString());
    }

}