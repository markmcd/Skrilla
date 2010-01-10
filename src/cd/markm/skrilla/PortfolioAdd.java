package cd.markm.skrilla;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;

public class PortfolioAdd extends PreferenceActivity
		implements OnPreferenceChangeListener {

	private static final String INSTITUTION_LIST = "institution_list";
	private static final String INSTITUTION_AUTH_PREFS = 
		"institution_auth_prefs";
	private static final String PORTFOLIO_NICKNAME = "portfolio_nickname";
	
	private static final String TAG = PortfolioAdd.class.getSimpleName();

	private static final int MENU_SAVE = Menu.FIRST;
	private static final int MENU_CANCEL = Menu.FIRST + 1;
	
	private ArrayList<Preference> mPrefs;	// per-institution Preferences
	private ListPreference mInstitutionList;
	private EditTextPreference mPortfolioNickname;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "Adding new portfolio");
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.layout.prefs_addnew);
		
		mPrefs = new ArrayList<Preference>();

		// set up the 'add institution' UI
		mInstitutionList = (ListPreference) findPreference(INSTITUTION_LIST);
		mInstitutionList.setValue(null);
		mPortfolioNickname = (EditTextPreference) 
			findPreference(PORTFOLIO_NICKNAME);
		mPortfolioNickname.setOnPreferenceChangeListener(this);
		mPortfolioNickname.setText(null);
		
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
		
		mInstitutionList.setEntries(ea);
		mInstitutionList.setEntryValues(ea);
		mInstitutionList.setOnPreferenceChangeListener(this);
	};
	
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == mInstitutionList) {
			institutionSelected(preference, newValue);
		}
		else if (preference == mPortfolioNickname) {
			mPortfolioNickname.setSummary((CharSequence) newValue);
		}
		return true;
	}

	private void institutionSelected(Preference preference, Object newValue) {
		// update the screen with new information
		if (newValue != null) {
			// update the summary of the institution preference
			preference.setSummary((CharSequence) newValue);
		
			// add the auth details for the selected institution
			PreferenceCategory pc = (PreferenceCategory)
				findPreference(INSTITUTION_AUTH_PREFS);
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
						EditTextPreference str = new EditTextPreference(pc.getContext());
						str.setDialogTitle(label);
						str.setTitle(label);
						p = str;
						break;
					case SECURE:
						EditTextPreference passPref = new EditTextPreference(pc.getContext());
						passPref.setDialogTitle(label);
						passPref.setTitle(label);
						// password mode
						passPref.getEditText().setTransformationMethod(
								new PasswordTransformationMethod());
						p = passPref;
						break;
					default:
						// TODO real exception
						throw new Exception("Unknown type of auth parameter");
					}

					p.setPersistent(false);
					p.setOrder(i++);
					pc.addPreference(p);
					mPrefs.add(p);
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
        		setResult(RESULT_OK);
        		finish();
                Toast.makeText(getApplicationContext(), 
        				R.string.portfolio_add_success, 
        				Toast.LENGTH_SHORT).show();
        		return true;
        	}
        	else {
        		Toast.makeText(getApplicationContext(), 
        				R.string.portfolio_add_fail, 
        				Toast.LENGTH_SHORT).show();
        		return true;
        	}
        case MENU_CANCEL:
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkSet(CharSequence s) {
    	if (s == null || s.length() < 1) {
    		return false;
    	}
    	return true;
    }
    
	private boolean validateAndSave() {
		// DB stuff
		DbHelper dbh = new DbHelper(getApplicationContext());
		SQLiteDatabase db = dbh.getWritableDatabase();
		db.beginTransaction();
		
		try {
			// first the standard preferences
			ListPreference instList = (ListPreference) findPreference(INSTITUTION_LIST);
			CharSequence institutionChars = instList.getEntry();
			EditTextPreference nicknamePref = (EditTextPreference) 
				findPreference(PORTFOLIO_NICKNAME);
			String nickname = nicknamePref.getText();
			
			// validate
			if (!(checkSet(institutionChars) || checkSet(nickname))) {
				Log.i(TAG, "Rejecting invalid details");
				db.endTransaction();
				db.close();
				return false;
			}
			
			String institution = institutionChars.toString();
			
			// save
			// TODO this should all be in some kind of DB layer
			Log.d(TAG, "Saving portfolio");
			ContentValues portfolio = new ContentValues();
			portfolio.put("Name", institution);
			portfolio.put("Class", FinancialInstitutionFactory
					.GetInstitution(institution).getClass().getName());
			// TODO check if the nickname is already in use
			portfolio.put("Nickname", nickname);
			long instId = db.insertOrThrow(
					DbHelper.DB_TABLE_PORTFOLIO, "Name", portfolio);
			Log.d(TAG, "Saved, portfolio ID: "+ instId);
			
			for (Preference p : mPrefs) {
				// validate
				// TODO some kind of validation call-back
				String value;
				if (p instanceof EditTextPreference) {
					EditTextPreference etp = (EditTextPreference) p;
					value = etp.getText();
					if (value == null || value.length() < 1) {
						Log.i(TAG, "Rejecting empty preference: "+
								p.getTitle());
						db.endTransaction();
						db.close();
						return false;
					}
				}
				else if (p instanceof ListPreference) {
					ListPreference lp = (ListPreference) p;
					CharSequence val = lp.getEntry();
					if (val == null || val.length() < 1) {
						Log.i(TAG, "Rejecting empty preference: "+
								p.getTitle());
						db.endTransaction();
						db.close();
						return false;
					}
					else {
						value = val.toString();
					}
				}
				else {
					Log.e(TAG, "Unknown preference type: " + p.getClass()
							.getName());
					db.endTransaction();
					db.close();
					throw new UnsupportedOperationException("Unsupported "+
							"Preference type");
				}

				// save
				Log.d(TAG, "Saving preference: "+ p.getTitle());
				ContentValues pref = new ContentValues();
				pref.put("PortfolioID", instId);
				pref.put("Field", p.getTitle().toString());
				pref.put("Value", value);
				long pid = db.insertOrThrow(DbHelper.DB_TABLE_AUTHDETAIL, 
						"Value", pref);
				Log.d(TAG, "Saved: "+ pid);
			}

			Log.d(TAG, "Success, committing transaction.");
			db.setTransactionSuccessful();
			db.endTransaction();
			db.close();
			return true;
		}
		catch (SQLException sqle) {
			// TODO probably want to pass this back up a little
			Log.e(TAG, "SQL problems afoot! "+ sqle.getMessage());
		} catch (Exception e) {
			// TODO this is the FinanicialInstitutionFactory unknown 
			// institution exception - fix this when that gets updated
			Log.e(TAG, "Exception (probably unknown institution): "+
					e.getMessage());
		}
		db.endTransaction();
		db.close();
		return false;
	}
}
