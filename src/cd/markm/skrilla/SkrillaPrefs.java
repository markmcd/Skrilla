package cd.markm.skrilla;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

public class SkrillaPrefs extends PreferenceActivity { 
	private static final String TAG = SkrillaPrefs.class.getSimpleName();
	
	private PreferenceScreen mAddNewPortfolio;
	
	private static final int REQUEST_ADD_PORTFOLIO = 1;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Loading preferences pane");

        addPreferencesFromResource(R.layout.prefs_main);

        mAddNewPortfolio = (PreferenceScreen) findPreference("add_new_portfolio");
        mAddNewPortfolio.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				startAddPortfolio();
				return true;
			}
		});
    }
    
    private void startAddPortfolio() {
    	Intent intent = new Intent(this, PortfolioAdd.class);
    	startActivityForResult(intent, REQUEST_ADD_PORTFOLIO);
    }
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_CANCELED) {
			Log.d(TAG, "Result cancelled");
		}
		else {
			if (requestCode == REQUEST_ADD_PORTFOLIO) {
				Log.d(TAG, "Add portfolio returned: "+resultCode);
			}
		}
	}
}