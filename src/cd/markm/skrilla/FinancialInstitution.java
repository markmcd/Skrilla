package cd.markm.skrilla;

import java.util.Map;

public abstract class FinancialInstitution {
	public enum DetailsType { STRING, SECURE }
	
	public abstract Map<String, DetailsType> authenticationDetails();
	
	public abstract String displayName();
	
	//public abstract 
}
