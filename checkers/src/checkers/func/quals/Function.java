package checkers.func.quals;

import checkers.quals.SubtypeOf;
import checkers.quals.TypeQualifier;
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
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
@SubtypeOf({})
public @interface Function {}