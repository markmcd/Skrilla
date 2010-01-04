package cd.markm.skrilla;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;

public class PortfolioAdd extends PreferenceActivity
		implements OnPreferenceChangeListener {
	private static final String TAG = PortfolioAdd.class.getSimpleName();
	private static final String INSTITUTION_LIST = "institution_list";
	private static final int MENU_SAVE = Menu.FIRST;
	private static final int MENU_CANCEL = Menu.FIRST + 1;
	
	private ArrayList<Preference> prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "Adding new portfolio");
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.layout.prefs_addnew);
		
		prefs = new ArrayList<Preference>();

		// set up the 'add institution' UI
		ListPreference listInstitutions = (ListPreference) findPreference(INSTITUTION_LIST);
		prefs.add(listInstitutions);
		
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
				// TODO maybe push the credential management screen into the
				// FinancialInstitution class?
				AuthDetails authDetails = 
					fi.authenticationDetails();
				int i = 0;
				for (String label : authDetails.getDefs()) {
					Preference p;
					
					// TODO these need to be styled properly
					switch (authDetails.getDef(label)) {
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
					prefs.add(p);
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		menu.add(0, MENU_SAVE, 0, R.string.menu_save)
        	.setIcon(android.R.drawable.ic_menu_save);
		menu.add(0, MENU_CANCEL, 0, R.string.menu_cancel)
			.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		
		return true;
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_SAVE:
        	if (validateAndSave()) {
                finish();
                return true;
        	}
        	// TODO let the user know what's wrong
        case MENU_CANCEL:
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

	private boolean validateAndSave() {
		for (Preference p : prefs) {
			// validate
			// TODO some kind of validation call-back
			if (p instanceof EditTextPreference) {
				EditTextPreference etp = (EditTextPreference) p;
				if (etp.getText().length() < 1) {
					return false;
				}
			}
			else if (p instanceof ListPreference) {
				ListPreference lp = (ListPreference) p;
				if (lp.getEntry() == null) {
					return false;
				}
			}

			// save
			
		}
		
		return true;

	}
}
