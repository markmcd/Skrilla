package cd.markm.skrilla;

import java.util.ArrayList;
import java.util.List;

import cd.markm.skrilla.banks.*;

public class FinancialInstitutionFactory {
	// can't do this with arrays unfortunately:
	// http://stackoverflow.com/questions/749425/how-do-i-use-generics-with-an-array-of-classes
	public static List<Class<? extends FinancialInstitution>> GetAvailableInstitutions() {
		List<Class<? extends FinancialInstitution>> banks = 
			new ArrayList<Class<? extends FinancialInstitution>>();

		banks.add(CommonwealthBank.class); 
		return banks;
	}
	
	// TODO make a proper exception for this
	public static FinancialInstitution GetInstitution(String displayName)
			throws Exception {
		if (displayName.equals("Commonwealth Bank")) {
			return new CommonwealthBank();
		}
		else {
			throw new Exception("Unknown Institution");
		}
			
	}
}
