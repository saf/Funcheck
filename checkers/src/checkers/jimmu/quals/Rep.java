package checkers.jimmu.quals;

import checkers.quals.SubtypeOf;
import java.lang.annotation.Documented;

/**
 * When an object is referenced by a field annotated with @Rep, it is considered
 * part of the object's representation when considering the purity and immutability
 * of the object.
 */
@Documented
@SubtypeOf({Safe.class})
public @interface Rep {}
