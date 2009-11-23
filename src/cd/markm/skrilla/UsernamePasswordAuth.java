package cd.markm.skrilla;

public class UsernamePasswordAuth extends AuthDetails {
	private static final String USERNAME = "Username / ID";
	private static final String PASSWORD = "Password";
	
	public UsernamePasswordAuth() {
		define(USERNAME, DetailsType.STRING);
		define(PASSWORD, DetailsType.SECURE);
	}
	
	public UsernamePasswordAuth(String u, String p) {
		this();
		setUsername(u);
		setPassword(p);
	}
	
	public void setUsername(String username) {
		try {
			putValue(USERNAME, username);
		} catch (NoSuchFieldException e) {
			// won't happen as long as the constructor is called
		}
	}
	
	public void setPassword(String password) {
		try {
			putValue(PASSWORD, password);
		} catch (NoSuchFieldException e) {
			// won't happen as long as the constructor is called
		}
	}
	
	public String getUsername() {
		String r = null;
		try {
			r = (String) getValue(USERNAME);
		} catch (NoSuchFieldException e) {
			// won't happen as long as the constructor is called
		}
		return r;
	}
}
