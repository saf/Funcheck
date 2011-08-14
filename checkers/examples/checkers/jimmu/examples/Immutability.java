package checkers.jimmu.examples;

import checkers.jimmu.quals.*;

/**
 * Example on how the Jimuva checker handles simple immutability.
 *
 * @author saf
 */
public class Immutability {

    public static class IntHolder {

        public Integer n;

        @Anonymous
        public IntHolder(Integer n) {
            this.n = n;
        }

        @ReadOnly
        public Integer get() {
            return n;
        }

        public void set(Integer n) {
            this.n = n;
        }

        public void copy(@Immutable IntHolder other) {
            this.n = other.n;
        }
    }

    public static void main(String[] args) {

        @Immutable IntHolder immutable = new /*@Immutable*/ IntHolder(21);
        immutable = new /*@Immutable*/ IntHolder(42);   /* This is OK! */
        @Mutable IntHolder mutable = immutable;         /* Error */
        mutable = new IntHolder(21);
        immutable = mutable;                            /* OK if upcasting allowed */

        System.out.println("Immutable: " + immutable.get().toString());
        System.out.println("Mutable: " + mutable.get().toString());

        immutable.set(0);        /* Error: non-@Readonly method called */
        mutable.set(1);

        immutable.n = 2;         /* Error */
        mutable.n = 3;

        immutable.copy(mutable); /* Error: non-@Readonly method called */
        mutable.copy(immutable); /* OK - argument of copy is read-only */
    }

}
