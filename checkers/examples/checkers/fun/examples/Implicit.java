/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.fun.examples;

import checkers.fun.quals.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * An example on how methods are implicitly annotated when using the 
 * @ImmutableClass annotation.
 *
 * @author saf
 */
public class Implicit {

    @ImmutableClass
    public static class A {
        public List<Integer> l; /* Can't be public in @Immutable class */

        public A(Integer n) {
            l = new LinkedList<Integer>();
            for (int i = n; i >= 1; i--) {
                l.add(i);
            }
            Sorter s = new Sorter();
            s.sort(this); /* Illegal: A(Integer) is implicitly @Anonymous */
        }

        public void set(List<Integer> nl) {
            this.l = nl;  /* Illegal: set() is implicitly @ReadOnly */
        }

        public List<Integer> get() {
            return new LinkedList<Integer>(l);
        }
    }

    public static class Sorter {
        void sort(A a) {
            List<Integer> al = a.get();
            Collections.sort(al);
            a.set(al);
        }
    }

    public static void main(String [] args) {
        A a = new A(5); /* Illegal: can't create @Mutable reference */
        @Immutable A b = new /*@Immutable*/ A(5);
        Sorter s = new Sorter();

        s.sort(b); /* Illegal: cannot pass @Immutable object as a @Mutable argument */
    }

}
