package checkers.fun.quals;

import checkers.quals.SubtypeOf;
import java.lang.annotation.*;

/**
 * A class is @ImmutableClass if all instances of the class are @Immutable.
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
@SubtypeOf({})
public @interface ImmutableClass {}