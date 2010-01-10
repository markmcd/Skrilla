package cd.markm.skrilla;

public class Portfolio {
	private int id;
	private String nickname;
	private String className;
	private String institution;
	
	public void setId(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getNickname() {
		return nickname;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getClassName() {
		return className;
	}
	public void setInstitution(String institution) {
		this.institution = institution;
	}
	public String getInstitution() {
		return institution;
	}
}
