package checkers.oigj.quals;

import java.lang.annotation.*;

import checkers.quals.SubtypeOf;
import checkers.quals.TypeQualifier;

@Documented
@Retention(RetentionPolicy.RUNTIME)
//@Target( { FIELD, LOCAL_VARIABLE, METHOD, PARAMETER, TYPE })
@TypeQualifier
@SubtypeOf({ World.class })
public @interface Dominator {}
