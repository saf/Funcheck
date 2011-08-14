/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.jimmu.quals;

import checkers.quals.SubtypeOf;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 *
 * @author saf
 */
@Documented
@SubtypeOf({})
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Local {}