/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.fun.quals;

import checkers.quals.SubtypeOf;
import checkers.quals.TypeQualifier;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author saf
 */
@Documented
@SubtypeOf({})
@TypeQualifier
@Retention(RetentionPolicy.CLASS)
public @interface OwnedBy {
    String value();
}
