package checkers.jimmu.examples;

import checkers.jimmu.quals.*;

/**
 * Example on using the @Rep annotation and immutability
 *
 * @author saf
 */
public class InternalRepresentation {

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

        public void safeCopy(@Safe IntHolder other) {
            this.n = other.n; /* Illegal -- other.n is @Safe! */
        }
    }

    public static class A {

        @Rep private IntHolder repHolder;
        IntHolder freeHolder;

        public A() {
            /*
             * The constructor call:
             *   new @Rep IntHolder(2)
             * is equivalent to Jimuva's
             *   new IntHolder<rdwr, this>(2)
             */
            repHolder = new /*@Rep*/ IntHolder(42);
            freeHolder = new IntHolder(21);
        }

        public A(A a) {
            this.repHolder = a.repHolder; /* Prohibited access to @Rep object. */
        }

        public A(@Rep IntHolder hld) {
            this.repHolder = hld;
            this.freeHolder = new IntHolder(21);
        }

        @ReadOnly
        public @Rep IntHolder process() {
            repHolder.copy(freeHolder); /* Can't modify repHolder here! */
            freeHolder.copy(repHolder); /* Can't pass object owned by "this" to a foreign method */
            this.nop(repHolder);        /* This is OK, though */

            repHolder.n = 10; /* Also, can't modify the state of repHolder */
            repHolder = new /*@Rep*/ IntHolder(0);
            return repHolder; /* Cannot return a @Rep object */
        }

        public @Rep IntHolder makeCopy() {
            A peer = new A(repHolder); /* Cannot pass @Rep as argument! */
            Integer val = peer.repHolder.get(); /* Cannot access @Rep of another object */
            repHolder.set(val);
            
            return repHolder; /* No can do! */
        }

        @ReadOnly
        void nop(@Rep IntHolder h) {}
    }

    public static void main(String [] args) {
        (new A()).process();
    }
    
}
