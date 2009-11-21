package cd.markm.skrilla;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.util.Log;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;

public class PortfolioAdd extends PreferenceActivity
		implements OnPreferenceChangeListener {
	private static final String TAG = PreferenceActivity.class.getSimpleName(); 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "Adding new portfolio");
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.layout.prefs_addnew);

		// set up the 'add institution' UI
		ListPreference listInstitutions = (ListPreference) findPreference("institution_list");
		
		List<CharSequence> entries = new ArrayList<CharSequence>();
		
		List<Class<? extends FinancialInstitution>> banks = 
			FinancialInstitutionFactory.GetAvailableInstitutions();
		for (Class<? extends FinancialInstitution> b : banks) {
			try {
				FinancialInstitution fi = b.newInstance();
				entries.add(fi.displayName());
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		CharSequence[] ea = new CharSequence[entries.size()];
		entries.toArray(ea);
		
		listInstitutions.setEntries(ea);
		listInstitutions.setEntryValues(ea);
		listInstitutions.setOnPreferenceChangeListener(this);
	};
	
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// update the screen with new information
		if (newValue != null) {
			// update the summary of the institution pref
			preference.setSummary((CharSequence) newValue);
		
			// add the auth details for the selected institution
			PreferenceCategory pc = (PreferenceCategory)
				findPreference("institution_auth_prefs");
			pc.removeAll();
			try {
				FinancialInstitution fi = FinancialInstitutionFactory
					.GetInstitution((String) newValue);
				Map<String, FinancialInstitution.DetailsType> authDetails = 
					fi.authenticationDetails();
				int i = 0;
				for (String label : authDetails.keySet()) {
					Preference p;
					
					// TODO these need to be styled properly
					switch (authDetails.get(label)) {
					case STRING:
						p = new EditTextPreference(pc.getContext());
						p.setTitle(label);
						break;
					case SECURE:
						// TODO make this a real password dialog
						p = new EditTextPreference(pc.getContext());
						p.setTitle(label);
						break;
					default:
						// TODO real exception
						throw new Exception("Unknown type of auth parameter");
					}
					
					p.setOrder(i++);
					pc.addPreference(p);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return true;
	}

}
