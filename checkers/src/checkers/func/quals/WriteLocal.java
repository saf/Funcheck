/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.func.quals;

import checkers.quals.SubtypeOf;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * A constructor is WriteLocal iff it does not modify the state
 * of other objects of the same class.
 *
 * @author saf
 */
@Documented
@Target({ElementType.CONSTRUCTOR})
@SubtypeOf({})
public @interface WriteLocal {}
