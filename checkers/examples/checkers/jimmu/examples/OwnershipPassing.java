package checkers.jimmu.examples;

import checkers.jimmu.quals.*;

/**
 * Example for ownership resolving.
 *
 * @author saf
 */
public class OwnershipPassing {

    public static class A {
        public static Integer st;
        public Integer owner;
        private @Rep Integer rep;
        private @Peer Integer peer;
        public @OwnedBy("owner") Integer owned;

        public void setRep(@Rep Integer n) {
            rep = n;
        }

        public void setPeer(@Peer Integer n) {
            this.peer = n;
        }

        public void setOwned(@OwnedBy("owner") Integer n) {
            this.owned = n;
        }

        public @OwnedBy("owner") Integer getOwned() {
            return owned;
        }

        public A() {
        }
    }


    static void process(A a) {
        @OwnedBy("a") Integer x = null;
        a.setRep(x);
        a.setPeer(x);

        @OwnedBy("a") A b = new /*@OwnedBy("a")*/ A();
        @OwnedBy("a") Integer y = null;
        b.setRep(y);
        b.setPeer(y);

        @OwnedBy("a.owner") Integer z = null;
        a.setOwned(z);

        a.owned = new /*@OwnedBy("a.owner")*/ Integer("0");
    }

    static Integer main(String[] args) {
        process(new A());

        A a = new A();
        @OwnedBy("a") Integer x = null;
        @OwnedBy("a.owner") Integer y = null;

        a.setRep(x);
        a.setOwned(y);

        @OwnedBy("a.owner") Integer z = a.getOwned();
        return a.st;
    }

}
