package cd.markm.skrilla;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class DbHelper extends SQLiteOpenHelper {
	private static final String TAG = DbHelper.class.getSimpleName();
	
	private static final String DB_NAME = "SkrillaDB";
	public static final String DB_TABLE_PORTFOLIO = "Portfolio";
	public static final String DB_TABLE_AUTHDETAIL = "AuthDetail";
	
	private static final String COL_PORTFOLIO_ID = "ID";
	private static final String COL_PORTFOLIO_NAME = "Name";
	private static final String COL_PORTFOLIO_CLASS = "Class";
	private static final String COL_PORTFOLIO_NICKNAME = "Nickname";

	private static final int DB_VERSION = 2;

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
		Log.i(TAG, "Upgrading DB from "+oldV+" to "+newV);
		for (int i = oldV; i <= newV; i++) {
			switch (i) {
			case 2:
				Log.i(TAG, "Adding nickname column");
				db.execSQL("ALTER TABLE " + DB_TABLE_PORTFOLIO +
						" ADD COLUMN Nickname TEXT");
				break;
			}
		}
	}

	public static List<Portfolio> GetPortfolios(SQLiteDatabase db) {
		String[] cols = { COL_PORTFOLIO_ID, COL_PORTFOLIO_NAME, 
				COL_PORTFOLIO_CLASS, COL_PORTFOLIO_NICKNAME };
		Cursor c = db.query(DB_TABLE_PORTFOLIO, cols, 
				null, null, null, null, null);
		
		List<Portfolio> portfolios = new ArrayList<Portfolio>();
		
		if (c != null) {
			while (c.moveToNext()) {
				Portfolio p = new Portfolio();
				p.setId(c.getInt(c.getColumnIndex(COL_PORTFOLIO_ID)));
				p.setClassName(c.getString(
						c.getColumnIndex(COL_PORTFOLIO_CLASS)));
				p.setInstitution(c.getString(
						c.getColumnIndex(COL_PORTFOLIO_NAME)));
				p.setNickname(c.getString(
						c.getColumnIndex(COL_PORTFOLIO_NICKNAME)));
				portfolios.add(p);
			}
		}
		
		return portfolios;
	}
}
