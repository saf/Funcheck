package checkers.jimmu.quals;

import checkers.quals.SubtypeOf;
import java.lang.annotation.Documented;

/**
 * Indicates that the reference surely points to 'this' within a method or constructor.
 *
 * @author saf
 */
@Documented
@SubtypeOf({MaybeThis.class})
public @interface This {}
