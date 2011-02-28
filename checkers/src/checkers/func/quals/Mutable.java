/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.func.quals;

import checkers.quals.DefaultQualifierInHierarchy;
import checkers.quals.ImplicitFor;
import checkers.quals.SubtypeOf;
import com.sun.source.tree.Tree.Kind;
import java.lang.annotation.*;

/**
 *
 *
 * @author saf
 */
@Documented
@Target({ElementType.LOCAL_VARIABLE, ElementType.PARAMETER, ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.CLASS)
@SubtypeOf({})
@DefaultQualifierInHierarchy
public @interface Mutable {}
