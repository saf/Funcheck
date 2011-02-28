package checkers.func.quals;

import checkers.quals.SubtypeOf;
import java.lang.annotation.*;

/**
 * An object is immutable if no other object can witness
 * two distinct states of the object.
 * A class is immutable if all objects are immutable.
 */
@Documented
@Target({ElementType.LOCAL_VARIABLE, ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
@SubtypeOf({Mutable.class})
public @interface Immutable {}