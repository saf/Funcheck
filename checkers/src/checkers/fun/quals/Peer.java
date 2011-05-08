/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.fun.quals;

import checkers.quals.SubtypeOf;
import java.lang.annotation.Documented;

/**
 * @Peer is equivalent to Jimuva's C<myowner>. A @Peer field is 
 * owned by the owner of the object.
 *
 * @author saf
 */
@Documented
@SubtypeOf({})
public @interface Peer {}
