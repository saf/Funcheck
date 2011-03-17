package checkers.fun.quals;

import checkers.quals.SubtypeOf;
import java.lang.annotation.*;

/**
 * A purely functional class is immutable and all its methods are purely
 * functional. 
 *
 * @see Immutable
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
@SubtypeOf({ImmutableClass.class})
public @interface PurelyFunctional {}
