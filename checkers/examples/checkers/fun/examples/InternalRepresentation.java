/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.fun.examples;

import checkers.fun.quals.*;

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

        @ReadOnly
        void process() {
            repHolder.copy(freeHolder); /* Can't modify repHolder here! */
            freeHolder.copy(repHolder); /* Can't pass object owned by <this> to method (???) */

            repHolder.n = 10; /* Also, can't modify the state of repHolder */
        }
    }

    public static void main(String [] args) {
        (new A()).process();
    }
    
}
