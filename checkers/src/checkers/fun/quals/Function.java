package checkers.fun.quals;

import checkers.quals.SubtypeOf;
import java.lang.annotation.*;

/**
 * Indicates that the annotated method is purely functional.
 *
 * Purely functional methods do not modify the program state
 * and yield the same result whenever they are called with
 * a given set of parameters.
 *
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.CLASS)
@SubtypeOf({ReadOnly.class})
public @interface Function {}