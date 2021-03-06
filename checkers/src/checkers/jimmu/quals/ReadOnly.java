package checkers.jimmu.quals;

import checkers.quals.SubtypeOf;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * A method is ReadOnly iff it does not modify the state of the object
 * it is called in. 
 *
 * @author saf
 */
@Documented
@Target({ElementType.METHOD})
@SubtypeOf({})
public @interface ReadOnly {}