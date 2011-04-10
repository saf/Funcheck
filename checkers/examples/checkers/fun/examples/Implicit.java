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
        private List<Integer> l;

        public A(Integer n) {
            l = new LinkedList<Integer>();
            for (int i = 1; i <= n; i++) {
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

}
