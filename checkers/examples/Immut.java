import checkers.func.quals.*;

@Immutable 
public class Immut {

    protected String s;

    public Immut() { s = ""; }
    public Immut(String s) { this.s = s; }

    public String getS() {
	return s;
    }

}