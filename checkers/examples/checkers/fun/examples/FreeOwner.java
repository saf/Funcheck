/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.fun.examples;

import checkers.fun.quals.AnyOwner;
import checkers.fun.quals.Immutable;
import checkers.fun.quals.Myaccess;
import checkers.fun.quals.OwnedBy;

/**
 *
 * @author saf
 */
public class FreeOwner {

    public static class IntList {
        public Integer n;
        public @Immutable IntList l;
    }

    public static class Slave {
        /* Toy method to check immutability */
        void modify() {}
    }

    public static class Master {

        public static class Inner {
            public Integer m;
            public static Integer s;
        }

        public Integer z;
        
    }

    private @Immutable Integer n;
    private @Myaccess Integer mn;
    private IntList list;

    public void foo(@OwnedBy("n") IntList x) {
        x.n = 0;
    }

    public Object create(@AnyOwner @Immutable Object x) {
        return new Object();
    }

    public void main(String[] args) {

        String[] foo = args;

        @OwnedBy("n")
        IntList c1 = new /*@OwnedBy("n")*/ IntList();
        c1.n = 0; /* Illegal: c1 is owned by n, which is @Immutable */

        @OwnedBy("m")
        String c1e = new /*@OwnedBy("m")*/ String("I am owned by m");

        @OwnedBy("mn")
        IntList c1m = new /*@OwnedBy("mn")*/ IntList();
        
        @OwnedBy("Master.z")
        String c2 = new /*@OwnedBy("Master.z")*/ String("I am owned by Master.z");

        @OwnedBy("Master.Inner.m") 
        String c3 = new /*@OwnedBy("Master.Inner.m")*/ String("I am owned by Master.Inner.m");

        @OwnedBy("Master.Inner.s")
        String c4 = new /*@OwnedBy("Master.Inner.s")*/ String("I am owned by Master.Inner.s");

        @OwnedBy("list.l.l")
        IntList c5 = new /*@OwnedBy("list.l.l")*/ IntList();
        c5.n = 0; /* Illegal */

        foo(c1); /* Illegal, c1 is @Immutable */

        Object x1 = create(c1);   /* Owned by n */
        Object x2 = create(list); /* Owned by world -- illegal, x2 is mutable */
        Object x3 = create(c1m);  /* Is @Myaccess -- illegal call */
    }
    

}
