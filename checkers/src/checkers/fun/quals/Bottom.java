/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.fun.quals;

import checkers.quals.DefaultQualifierInHierarchy;
import checkers.quals.SubtypeOf;
import java.lang.annotation.Documented;

/**
 * Dummy type annotation to be used for declared types. It needs not appear in user code. 
 *
 * @author saf
 */
@Documented
@SubtypeOf({})
@DefaultQualifierInHierarchy
public @interface Bottom {}