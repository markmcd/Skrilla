package cd.markm.skrilla;

import java.util.HashMap;
import java.util.Map;

public class AuthDetails {
	public enum DetailsType { STRING, SECURE }
	
	protected Map<String, DetailsType> details
		= new HashMap<String, DetailsType>();
	
	protected Map<String, Object> values
		= new HashMap<String, Object>();

	public AuthDetails() {
	}
	
	public void define(String displayName, DetailsType type) {
		details.put(displayName, type);
	}
	
	public Iterable<String> getDefs() {
		return details.keySet();
	}
	
	public DetailsType getDef(String displayName) {
		return details.get(displayName);
	}
	
	public boolean isDefined(String displayName) {
		return details.containsKey(displayName);
	}
	
	public void putValue(String key, Object value) throws NoSuchFieldException {
		if (details.containsKey(key)) {
			values.put(key, value);
		}
		else {
			throw new NoSuchFieldException("Key "+key+" has not been defined.");
		}
	}
	
	public Object getValue(String key) throws NoSuchFieldException {
		if (values.containsKey(key)) {
			return values.get(key);
		}
		else {
			throw new NoSuchFieldException("Key "+key+" has not been defined.");
		}
	}
}
