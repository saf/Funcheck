package checkers.jimmu.examples;

import checkers.jimmu.quals.*;

/**
 * Example for resolving of the [@Myaccess] access rights variable.
 *
 * @author saf
 */
public class AccessResolving {

    public static class A {
        @Myaccess Integer n;
        @Mutable A a;
        @Myaccess A b;

        public A() {}

        @ReadOnly
        void foo(@Myaccess Integer k) {
            n = k;
        }

        void bar() {
            a.b.n = n;
        }
    }

    static void main(String [] args) {
        @Immutable A a = new /*@Immutable*/ A();
        A b = new A();

        @Mutable Integer m = 0;
        @Immutable Integer i = 0;

        a.foo(m);
        a.foo(i);
        b.foo(m);
        b.foo(i);
    }

}
