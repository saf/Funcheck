package checkers.jimmu.quals;

import checkers.quals.SubtypeOf;
import java.lang.annotation.Documented;

/**
 * @Peer is equivalent to Jimuva's C<myowner>. A @Peer field is 
 * owned by the owner of the object.
 *
 * @author saf
 */
@Documented
@SubtypeOf({Safe.class})
public @interface Peer {}
