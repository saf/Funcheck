package checkers.jimmu.examples;

import checkers.jimmu.quals.*;

/**
 * Example for the encapsulation of representation in JimmuChecker.
 *
 * @author saf
 */
public class EncapProtection {

    public static class Value {
        public @Immutable Integer v;

        public Value(@Immutable Integer v) {
            this.v = v;
        }

    }

    public static class A {
        @Peer Value value;

        void set(@Peer Value v) {
            this.value = v;
        }

        @ReadOnly void foo() {
            value.v = 0;
        }
    }

    public static class B {
        @Rep A a;

        public B() {
            @Rep Value v = new /*@Rep*/ Value(1);
            a = new /*@Rep*/ A();
            a.set(v);
        }

        @ReadOnly void bar() {
            a.foo();
        }
    }



}
