package checkers.jimmu.quals;

import checkers.quals.DefaultQualifierInHierarchy;
import checkers.quals.ImplicitFor;
import checkers.quals.SubtypeOf;
import com.sun.source.tree.Tree;
import java.lang.annotation.*;

/**
 * A class is @ImmutableClass if all instances of the class are @Immutable.
 */
@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
@SubtypeOf({})
@ImplicitFor(trees = {Tree.Kind.CLASS})
public @interface MutableClass {}