package checkers.jimmu.quals;

import checkers.quals.SubtypeOf;

/**
 * Indicates that the reference may not point to 'this' within a method or constructor.
 *
 * @author saf
 */
@SubtypeOf({MaybeThis.class})
public @interface NotThis {}
