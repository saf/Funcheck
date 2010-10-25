@TypeQualifier
@Target({ElementType.TYPE_PARAMETER, 
         ElementType.TYPE_USE})
@SubtypeOf( { Nullable.class } )
public @interface NonNull { }