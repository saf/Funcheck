/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.fun.examples;

import checkers.fun.quals.*;
import java.util.Random;

/**
 * Example on enforcing anonymity of constructors.
 *
 * @author saf
 */
public class Anonymity {

    
    public static class Multiplier {
        Integer result;

        @Anonymous
        public Multiplier(int a, int b) {
            chainedMultiply(a, b); /* Can't call that -- not @Anonymous */
            multiply(a, b);        /* OK */
            System.err.println(getResult().toString());
        }

        public Multiplier chainedMultiply(int a, int b) {
            result = a * b;
            return this;
        }

        @Anonymous
        public void multiply(int a, int b) {
            result = a * b;
        }

        @Anonymous
        public Integer getResult() {
            return result;
        }

        @Anonymous
        public Integer getRandomResult() {
            @Immutable Multiplier m;
            Random r = new Random();
            if (r.nextBoolean()) {
                m = new /*@Immutable*/ Multiplier(r.nextInt(100), r.nextInt(100));
            } else {
                m = this;
            }
            DangerousClass.doDangerousStuffWith(m); /* m may be this -- not anonymous */
            return m.getResult();
        }
    }

    public static class DangerousClass {
        static void doDangerousStuffWith(Multiplier m) {}
    }

    public static void main(String [] args) {
        System.err.println((new /*@Immutable*/ Multiplier(2, 3)).getRandomResult().toString());
    }
}
