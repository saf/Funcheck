/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.fun.quals;

import checkers.quals.SubtypeOf;
import java.lang.annotation.Documented;

/**
 * Equivalent of Jimuva's C<world>. Does not need to be used in user code, exists for technical 
 * purposes. 
 *
 * @author saf
 */
@SubtypeOf({AnyOwner.class})
@Documented
public @interface World {}
