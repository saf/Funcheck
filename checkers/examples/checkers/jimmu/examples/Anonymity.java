package checkers.jimmu.examples;

import checkers.jimmu.quals.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Example on enforcing anonymity of constructors.
 *
 * @author saf
 */
public class Anonymity {

    static Random r;

    private void init() { r = new Random(); }

    public static class A {

        List<Integer> list;
        Set<Integer> set;

        public A() {
            list = new LinkedList<Integer>();
            set  = new HashSet<Integer>();
        }

        @Anonymous
        public A(List<Integer> l) {
            Initializer i = new Initializer();
            /* The Initializer 'i' might observe the state of the constructed object
             * while it is not yet fully initialized. Thus, the call is
             * prohibited in an @Anonymous constructor */
            i.initialize(this, l);
            list = l;
        }

        @Anonymous
        public A(Integer n) {
            this.prepareSet(n); /* OK */
            prepareSet(n);      /* OK */
            prepareList(n);     /* Error: the called method is not @Anonymous
                                   and might leak references to a partially
                                   constructed object. */
        }

        @Anonymous
        protected void prepareSet(Integer n) {
            for (int i = 1; i <= n; i++)
                set.add(i);
        }

        protected void prepareList(Integer n) {
            for (int i = 1; i <= n; i++)
                list.add(i);
        }

        @ReadOnly @Anonymous
        public Set<Integer> getSet() { return set; }
    }

    public static class Initializer {

        void initialize(A a, List<Integer> l) {
            for (Integer i : l)
                a.getSet().add(i);
        }

    }

    public static void main(String [] args) {
        /* This call issues a warning: 'a' is constructed using a non-anonymous
         * constructor. Hence, the state of the partially-constructed 'a' may leak
         * outside the constructor. */
        @Immutable A a = new /*@Immutable*/ A();

        @Immutable A b = new /*@Immutable*/ A(5);
        System.out.println(b.getSet().toString());
    }
    
    
}
