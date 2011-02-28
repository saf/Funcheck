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
 * A constructor is Anonymous if it does not leak [this] to foreign methods,
 * doesn't return [this] or assign [this] to fields, and does not
 * call non-anonymous methods.
 *
 * @author saf
 */
@Documented
@Target({ElementType.CONSTRUCTOR})
@SubtypeOf({})
public @interface Anonymous {}
