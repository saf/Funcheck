package checkers.jimmu.quals;

import checkers.quals.SubtypeOf;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A @Myaccess reference is resolved to @Mutable or @Immutable depending
 * on the access rights of the enclosing object.
 *
 * @author saf
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@SubtypeOf({Bottom.class})
public @interface Myaccess {}
