/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.jimmu.examples;

import checkers.jimmu.quals.*;

/**
 * Example on immutability and @Rep of arrays.
 *
 * @author saf
 */
public class Arrays {

    public static class FunnyClass {

        @Rep @Immutable FunnyClass /*@Rep*/ [] fc;
        Integer /*@Immutable*/ /*@Rep*/ [] ints;
        Integer /*@Rep*/ [] /*@Rep*/ [] rrintints;
        Integer /*@Rep*/ [] [] rintints;

        @Anonymous
        public FunnyClass() {}

        public FunnyClass(Integer n) {
            /* Issues a warning if FunnyClass() is not @Anonymous */
            fc = new /*@Rep*/ /*@Immutable*/ FunnyClass /*@Rep*/ [2];

            ints = new Integer /*@Rep*/ /*@Immutable*/ [n];
            /* TODO How to initialize @Immutable arrays? */
            for (int i = 0; i < n; i++)
                ints[i] = 1;

            rrintints = new Integer [2] [2]; /* Error - no @Rep */
            rrintints = new Integer /*@Rep*/ [2] /*@Rep*/ [2]; /* OK */

            rintints = new Integer /*@Rep*/ [2] /*@Rep*/ [2]; /* Error: excess @Rep */
            rintints = new Integer /*@Rep*/ [2] [2]; /* OK */

            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 2; j++) {
                    rrintints[i][j] = 1;
                    rintints[i][j] = 1;
                }
            }
        }

        @ReadOnly
        public void modify() {
            ints[0] = 2;         /* Error: ints is immutable */
            ints = new Integer /*@Immutable*/ /*@Rep*/ [2]; /* Error: modifying field */

            rrintints[1] = new Integer /*@Rep*/ [2]; /* Error: cannot change the @Rep array rrintints */
            rintints[1] = new Integer [2];  /* Error: cannot change the @Rep array rintints */

            rrintints[0][0] = 2; /* Error: cannot change a field of @Rep [] @Rep [] */
            rintints[0][0] = 2;  /* OK: the values in rintints[0] don't belong to the internal state */
        }
    }

    static void main(String [] args) {
        FunnyClass f = new FunnyClass(3);
        f.modify();
    }
}
