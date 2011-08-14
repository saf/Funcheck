/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.jimmu.examples;

import checkers.jimmu.quals.*;

/**
 * Test Safe and AnyOwner parameter annotations.
 *
 * @author saf
 */
public class OwnerPolymorphism {

    public static class Concat {
        void concat(@AnyOwner String s1, @AnyOwner String s2) {
            s1.concat(s2);
        }

        void safeConcat(@Safe String s1, @AnyOwner String s2) {
            s1.concat(s2);  /* This is actually illegal */
        }
    }

    void test(String [] args) {
        @Rep String s1 = new /*@Rep*/ String("foo");
        String s2 = " bar";
        String s3 = " bar";
        Concat c = new Concat();
        c.concat(s1, s3); /* Illegal: parameter not Safe */
        c.concat(s2, s3);
        c.safeConcat(s1, s3); /* OK */
    }
}
