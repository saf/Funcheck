import checkers.nullness.quals.*;

/*
 * @skip-test
 * This tests ensure that Pure and AssertNonNullIfTrue methods
 * are verified
 */
public class AssertIfTrueTestSimple {

  protected int @Nullable [] values;

  @AssertNonNullIfTrue("values")
  @Pure
  public boolean repNulledBAD() {
    //:: (some.error.here)
    return values == null;
  }

  @AssertNonNullIfFalse("values")
  @Pure
  public boolean repNulled() {
    return values == null;
  }

  public void addAll(AssertIfTrueTestSimple s) {
    if (repNulled())
      return;
    @NonNull Object x = values;

    if (s.repNulled()) {
      @NonNull Object y = values;
    }
  }

}
