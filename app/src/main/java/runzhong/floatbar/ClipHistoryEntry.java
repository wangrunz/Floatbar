package runzhong.floatbar;

import android.provider.BaseColumns;

/**
 * Created by wrz19 on 3/27/2017.
 */

public final class ClipHistoryEntry {
    private ClipHistoryEntry(){};

    public static class ClipEntry implements BaseColumns{
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_DATA = "data";
        public static final String COLUMN_NAME_UPDATE_TIME = "update_time";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_FAVORITE = "favorite";
    }
}
