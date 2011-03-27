import checkers.fun.quals.*;
import java.util.Arrays;

@ImmutableClass
public class Immut {

    static class IntHolder {
        public Integer i;
        public Integer @Rep [] ar;
        @Rep public IntHolder hld;

        @Anonymous 
        public IntHolder() {
            IntHolder alias;
            alias = this;
            alias = new IntHolder();
            hld = new @Rep IntHolder();
            hld = this;
            i = get();
            i = this.get();
            i = hld.get();
            i = alias.get();
            ar = new Integer @Rep [3];
            doDangerousStuffWith(this);
            doDangerousStuffWith(alias);
            hld.doDangerousStuffWith(this);
            hld.doDangerousStuffWith(alias);
        }

        void set(int j) {
            i = j;
        }

        @ReadOnly
        @Anonymous
        Integer get() {
            i = 0; /* Obvious error */
            ar = new Integer @Rep [3];  /* Another obvious error */
            ar[0] = 3;
            Arrays.asList(ar);
            // hld.set(3); /* Error! */
            Integer x = hld.get();
            return x;
        };

        void doDangerousStuffWith(IntHolder arg) {}
        void doDangerousStuffWithArray(Integer[] arg) {}

        @Rep IntHolder getHolder() {
            @Rep IntHolder h = hld;
            return h;
        }

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
        @Immutable IntHolder hi = new @Immutable IntHolder();
        g.copy(hi);
        g.get();
        hi.get();
    }

}