/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.jimmu.quals;

import checkers.quals.DefaultQualifierInHierarchy;
import checkers.quals.ImplicitFor;
import checkers.quals.SubtypeOf;
import com.sun.source.tree.Tree.Kind;
import java.lang.annotation.*;

/**
 * General type of object references.
 *
 * @author saf
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@SubtypeOf({Bottom.class})
public @interface Mutable {}
