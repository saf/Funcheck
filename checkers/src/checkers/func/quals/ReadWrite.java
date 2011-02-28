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
 * The general type annotation of methods
 *
 * @author saf
 */
@Documented
@Target({ElementType.METHOD})
@SubtypeOf({})
public @interface ReadWrite {}