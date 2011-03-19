import checkers.fun.quals.*;

@ImmutableClass
public class Immut {

    static class IntHolder {
        public Integer i;
        IntHolder hld;

        public IntHolder() {
            i = 0;
            hld = new IntHolder();
        }

        void set(int j) {
            i = j;
        }

        @ReadOnly
        Integer get() {
            i = 0;
            this.hld.i = 0;
            return i;
        };

        void copy(@Immutable IntHolder h) {
            set(h.get());
        }
    }

    public static void reset(IntHolder i) {
        i.set(0);
    }

    public static void main(String[] args) {
        // @Mutable IntHolder foo = h; /* Error */
        IntHolder g = new IntHolder();
        @Immutable IntHolder hi = new IntHolder();
        g.copy(hi);
        g.get();
        hi.get();
    }

}