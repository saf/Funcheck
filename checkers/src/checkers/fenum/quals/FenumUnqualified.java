package checkers.fenum.quals;

import java.lang.annotation.*;

import checkers.quals.DefaultQualifierInHierarchy;
import checkers.quals.SubtypeOf;
import checkers.quals.TypeQualifier;

/**
 * 
 * @author wmdietl
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target( {} )
@TypeQualifier
@SubtypeOf( { FenumTop.class } )
@DefaultQualifierInHierarchy
public @interface FenumUnqualified {}
