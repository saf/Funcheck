package checkers.fun.quals;

import checkers.quals.ImplicitFor;
import checkers.quals.SubtypeOf;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import java.lang.annotation.*;


/**
 * An object is Immutable if its state cannot be modified. 
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@SubtypeOf({Mutable.class})
public @interface Immutable {}