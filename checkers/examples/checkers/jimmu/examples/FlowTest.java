/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.jimmu.examples;

import checkers.jimmu.quals.Anonymous;

/**
 *
 * @author saf
 */
public class FlowTest {

    @Anonymous
    public FlowTest process(FlowTest c) {
        
        FlowTest z;
        if (false) {
            z = this;
        } else {
            z = c;
        }
        z.bar(0);
        return this;
    }

    @Anonymous
    public FlowTest foo(Integer c) {
        return new FlowTest();
    }

    public FlowTest bar(Integer c) {
        return new FlowTest();
    }

}
