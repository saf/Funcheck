/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.fun.quals;

import checkers.quals.SubtypeOf;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * If a method has an @AnyOwner parameter, objects of any ownership
 * specified by @OwnedBy can be passed on to this parameter. 
 *
 * If a variable or field is annotated by @AnyOwner, values of any
 * @OwnedBy ownership can be assigned to it.
 * 
 * This is similar to @Safe, but the checker does not verify the 
 * safety (i.e. lack of static links created by the method) of this value.
 *
 * @author saf
 */
@Target({ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.FIELD})
@SubtypeOf({})
public @interface AnyOwner {}
