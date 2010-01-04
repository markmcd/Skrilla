package cd.markm.skrilla;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {
	private static final String TAG = DbHelper.class.getSimpleName();
	
	private static final String DB_NAME = "SkrillaDB";
	private static final String DB_TABLE_PORTFOLIO = "Portfolio";
	private static final String DB_TABLE_AUTHDETAIL = "AuthDetail";
	private static final int DB_VERSION = 1;

	private DbHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
	}
	
	public DbHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG, "Creating database for the first time");
		db.execSQL(
			"CREATE TABLE IF NOT EXISTS " +
			DB_TABLE_PORTFOLIO +
			"(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
			" Name TEXT, " +
			" Class TEXT)"
		);
		
		db.execSQL(
			"CREATE TABLE IF NOT EXISTS " +
			DB_TABLE_AUTHDETAIL +
			"(PortfolioID INTEGER, " +
			" Field TEXT, " +
			" Value TEXT, " +
			" CONSTRAINT FK_PortfolioID FOREIGN KEY (PortfolioID) REFERENCES "+
			DB_TABLE_PORTFOLIO + ")"
		);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
		// TODO no updgrade required yet
	}

}
