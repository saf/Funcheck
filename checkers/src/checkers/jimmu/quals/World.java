package checkers.jimmu.quals;

import checkers.quals.SubtypeOf;
import java.lang.annotation.Documented;

/**
 * Equivalent of Jimuva's C<world>. The default ownership annotation of object references. 
 * Does not need to be used in user code.
 *
 * @author saf
 */
@SubtypeOf({AnyOwner.class})
@Documented
public @interface World {}
