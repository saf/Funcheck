/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.fun.quals;

import checkers.quals.SubtypeOf;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * A parameter or variable is safe if no static aliases to them can be created. 
 * This is used to mimic Jimuva's ownership-polymorphic methods.
 *
 * @author saf
 */
@Target({ElementType.PARAMETER, ElementType.LOCAL_VARIABLE})
@SubtypeOf({})
@Documented
public @interface Safe {}
