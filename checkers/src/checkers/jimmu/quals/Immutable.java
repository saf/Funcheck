package checkers.jimmu.quals;

import checkers.quals.SubtypeOf;
import java.lang.annotation.*;


/**
 * An object is Immutable if its state cannot be modified. 
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@SubtypeOf({Bottom.class})
public @interface Immutable {}