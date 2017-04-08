package runzhong.floatbar;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by wrz19 on 3/27/2017.
 */

public class ClipHistoryDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ClipHistory.db";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_TABLE = "CREATE TABLE "+ ClipHistoryEntry.ClipEntry.TABLE_NAME+" (" +
            ClipHistoryEntry.ClipEntry._ID+" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            ClipHistoryEntry.ClipEntry.COLUMN_NAME_TITLE + " TEXT NOT NULL DEFAULT 'New Clip'," +
            ClipHistoryEntry.ClipEntry.COLUMN_NAME_FAVORITE +" INTEGER NOT NULL DEFAULT 0,"+
            ClipHistoryEntry.ClipEntry.COLUMN_NAME_DATA+" TEXT NOT NULL," +
            ClipHistoryEntry.ClipEntry.COLUMN_NAME_UPDATE_TIME+" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP)";

    private static final String DROP_TABLE = "DROP TABLE IF EXISTS "+ ClipHistoryEntry.ClipEntry.TABLE_NAME;

    public ClipHistoryDbHelper(Context context) {
        super(context,DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try{
            db.execSQL(CREATE_TABLE);
            ContentValues contentValues = new ContentValues();
            contentValues.put(ClipHistoryEntry.ClipEntry.COLUMN_NAME_TITLE,"Welcome");
            contentValues.put(ClipHistoryEntry.ClipEntry.COLUMN_NAME_DATA,"Feel free to manager your clipboard!");
            db.insert(ClipHistoryEntry.ClipEntry.TABLE_NAME,null,contentValues);
        }catch (Exception e){

        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE);
        onCreate(db);
    }
}
