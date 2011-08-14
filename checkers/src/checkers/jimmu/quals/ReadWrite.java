/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package checkers.jimmu.quals;

import checkers.quals.ImplicitFor;
import checkers.quals.SubtypeOf;
import com.sun.source.tree.Tree;
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
@ImplicitFor(trees = {Tree.Kind.METHOD})
public @interface ReadWrite {}