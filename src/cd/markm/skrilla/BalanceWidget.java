package cd.markm.skrilla;

import cd.markm.skrilla.banks.CommonwealthBank;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;

public class BalanceWidget extends AppWidgetProvider {
	private static final String TAG = BalanceWidget.class.getSimpleName();
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		// run from a service to avoid "app not responding" errors
		Log.d(TAG, "Got onUpdate notice, starting UpdateService");
		context.startService(new Intent(context, UpdateService.class));
	}
	
	public static class UpdateService extends Service {
		private static final String TAG = UpdateService.class.getSimpleName();
		
		@Override
		public void onStart(Intent intent, int startId) {
			Log.i(TAG, "Starting UpdateService");
			RemoteViews updateViews = buildUpdate(this);
			
			AppWidgetManager manager = AppWidgetManager.getInstance(this);
			
			manager.updateAppWidget(
					new ComponentName(this, BalanceWidget.class), updateViews); 
		}
		
		// loosely based on the SimpleWiktionary example from Google
		public RemoteViews buildUpdate(Context context) {
			Log.i(TAG, "Building update");
			RemoteViews rv = new RemoteViews(context.getPackageName(), 
					R.layout.widget_balance);
			
			// TODO load selected account details from prefs page
			FinancialInstitution bank = new CommonwealthBank();
			UsernamePasswordAuth creds = new UsernamePasswordAuth("u", "p");
			bank.getAccounts(creds);
			
			// set the date & time 
			Time now = new Time();
			now.setToNow();
			// this format is 'Nov 12, 8:31 am'
			rv.setTextViewText(R.id.message, now.format("%b %e, %l:%M %P"));
			
			return rv;
		}
		
		@Override
		public IBinder onBind(Intent intent) {
			// binding allows external processes to communicate with this
			// service - we don't want that so just null it.
			return null;
		}
		
	}
}
