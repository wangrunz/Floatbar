package runzhong.floatbar;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by wrz19 on 3/27/2017.
 */

public class ClipHistoryDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ClipHistory.db";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "+ ClipHisotryEntry.ClipEntry.TABLE_NAME+" (" +
            ClipHisotryEntry.ClipEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            ClipHisotryEntry.ClipEntry.COLUMN_NAME_DATA+" TEXT NOT NULL," +
            ClipHisotryEntry.ClipEntry.COLUMN_NAME_UPDATE_TIME+" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)";

    private static final String DROP_TABLE = "DROP TABLE IF EXISTS "+ ClipHisotryEntry.ClipEntry.TABLE_NAME;

    public ClipHistoryDbHelper(Context context) {
        super(context,DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }
}
