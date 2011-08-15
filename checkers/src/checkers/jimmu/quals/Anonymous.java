package checkers.jimmu.quals;

import checkers.quals.SubtypeOf;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * A constructor is Anonymous if it does not leak [this] to foreign methods,
 * doesn't return [this] or assign [this] to fields, and does not
 * call non-anonymous methods.
 *
 * @author saf
 */
@Documented
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
@SubtypeOf({})
public @interface Anonymous {}
