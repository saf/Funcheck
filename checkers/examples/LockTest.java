import checkers.lock.quals.*;

public class LockTest {
    
    private String password;

    @GuardedBy("this.password")
    private int passwordHash;

    @Holding("this.password")
    private void doChangePassword(String p) {
	password = p;
	passwordHash = p.hashCode();
    }

    public int getPasswordHash() {
	return this.passwordHash;
    }
    
    public void changePassword(String p) {
	doChangePassword(p);
    }
}
