package checkers.jimmu.quals;

import checkers.quals.SubtypeOf;
import checkers.quals.TypeQualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that a reference is owned by the object described by the String value. 
 *
 * @author saf
 */
@Documented
@SubtypeOf({AnyOwner.class})
@TypeQualifier
@Retention(RetentionPolicy.CLASS)
public @interface OwnedBy {
    String value();
}
