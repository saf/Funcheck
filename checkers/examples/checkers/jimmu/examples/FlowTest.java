package checkers.jimmu.examples;

import checkers.jimmu.quals.Anonymous;

/**
 * Testing inferring [this] reference tracking annotations [@NotThis], [@MaybeThis], [@This].
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
