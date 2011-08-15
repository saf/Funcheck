package checkers.jimmu.quals;

import checkers.quals.SubtypeOf;
import java.lang.annotation.*;

/**
 * General type of object references.
 *
 * @author saf
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@SubtypeOf({Bottom.class})
public @interface Mutable {}
