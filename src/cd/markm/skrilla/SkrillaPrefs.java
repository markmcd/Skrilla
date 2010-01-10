package cd.markm.skrilla;

import java.util.List;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

public class SkrillaPrefs extends PreferenceActivity { 
	private static final String PORTFOLIO_LIST = "portfolio_list";

	private static final String ADD_NEW_PORTFOLIO = "add_new_portfolio";

	private static final String TAG = SkrillaPrefs.class.getSimpleName();
	
	private PreferenceScreen mAddNewPortfolio;
	
	private static final int REQUEST_ADD_PORTFOLIO = 1;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Loading preferences pane");

        addPreferencesFromResource(R.layout.prefs_main);

        mAddNewPortfolio = (PreferenceScreen) 
        	findPreference(ADD_NEW_PORTFOLIO);
        mAddNewPortfolio.setOnPreferenceClickListener(
        	new OnPreferenceClickListener() {
				public boolean onPreferenceClick(Preference preference) {
					startAddPortfolio();
					return true;
				}
		});
        
        updatePortfolioList();
    }
    
    private void startAddPortfolio() {
    	Intent intent = new Intent(this, PortfolioAdd.class);
    	startActivityForResult(intent, REQUEST_ADD_PORTFOLIO);
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//super.onActivityResult(requestCode, resultCode, data);
		switch(resultCode) {
		case RESULT_CANCELED:
			Log.d(TAG, "Result cancelled");
			break;
			
		case RESULT_OK:
			if (requestCode == REQUEST_ADD_PORTFOLIO) {
				updatePortfolioList();
			}
			break;
		}
	}
	
	private void updatePortfolioList() {
		DbHelper dbh = new DbHelper(getApplicationContext());
		SQLiteDatabase db = dbh.getReadableDatabase();
		
		List<Portfolio> portfolios = DbHelper.GetPortfolios(db);
		
		PreferenceCategory pc = (PreferenceCategory) 
			findPreference(PORTFOLIO_LIST);
		
		for (Portfolio p : portfolios) {
			EditTextPreference etp = new EditTextPreference(pc.getContext());
			etp.setTitle(p.getNickname());
			etp.setSummary(p.getInstitution());
			etp.setEnabled(false);
			pc.addPreference(etp);
		}
	}
}