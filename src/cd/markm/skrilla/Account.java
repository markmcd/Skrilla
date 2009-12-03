package cd.markm.skrilla;

import java.util.Currency;

// this is a bank account, of which every bank login should have at least one
// (to be useful anyway)

public class Account {
	private String name;
	private String identifier;
	private Double balance;
	private Currency unit;
	
	public Account(String name, String identifier, Currency unit, Double balance) {
		super();
		this.name = name;
		this.identifier = identifier;
		this.balance = balance;
		this.unit = unit;
	}

	public Account() {
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public Double getBalance() {
		return balance;
	}

	public void setBalance(Double balance) {
		this.balance = balance;
	}

	public Currency getUnit() {
		return unit;
	}

	public void setUnit(Currency unit) {
		this.unit = unit;
	}
	
}
