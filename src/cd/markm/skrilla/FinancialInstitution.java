package cd.markm.skrilla;

//the goal here is to define an abstract financial institution (i.e. a template for a bank)
//each implementation will be responsible for connecting to the bank & retrieving the kinds of
//accounts listed with the user and eventually retrieving the balances.
//the user's selected bank & account type will be stored outside of the code so I need to be
//able to retrieve and instantiate this stuff from a string or some other identifier

import java.util.List;

public abstract class FinancialInstitution {
	
	public abstract AuthDetails authenticationDetails();
	
	public abstract String displayName();
	
	public abstract List<Account> getAccounts(AuthDetails ad); 
}
