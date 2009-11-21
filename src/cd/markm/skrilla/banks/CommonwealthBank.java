package cd.markm.skrilla.banks;

import java.util.HashMap;
import java.util.Map;

import cd.markm.skrilla.FinancialInstitution;

public class CommonwealthBank extends FinancialInstitution {

	@Override
	public Map<String, DetailsType> authenticationDetails() {
		Map<String, DetailsType> authDetails = new HashMap<String, DetailsType>();
		
		authDetails.put("Client number", DetailsType.STRING);
		authDetails.put("Password", DetailsType.SECURE);
		
		return authDetails;
	}
	
	public String displayName() { return "Commonwealth Bank"; }

}
