package runzhong.floatbar;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class FloatingWindowService extends Service {
    private static final String TAG = "ClipboardManager";
    private boolean fab_status;
    private ClipHistoryDbHelper mDbHelper;
    private SQLiteDatabase db;
    private static final int NOTIFICATION_ID = 100;
    private WindowManager wm;
    private WindowManager.LayoutParams parameters;
    private OverlayView overlayView;
    private ClipboardManager mClipboardManager;
    private ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
        @Override
        public void onPrimaryClipChanged() {
            ClipData newClip = mClipboardManager.getPrimaryClip();

            int count = newClip.getItemCount();
            for (int i=0;i<count;i++){
                if (newClip.getItemAt(i).getText()==null){
                    break;
                }
                String text = newClip.getItemAt(i).getText().toString();

                String[] projection = {
                        ClipHisotryEntry.ClipEntry._ID
                };

                String selection = ClipHisotryEntry.ClipEntry.COLUMN_NAME_DATA + " = ?";
                String[] selectionArgs = {text};

                String sortOrder = ClipHisotryEntry.ClipEntry.COLUMN_NAME_UPDATE_TIME + " DESC";

                Cursor cursor = db.query(
                        ClipHisotryEntry.ClipEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                if (cursor.getCount()>0){
                    while (cursor.moveToNext()){
                        long id = cursor.getLong(cursor.getColumnIndex(ClipHisotryEntry.ClipEntry._ID));
                        //update table count+1
                        String query = "UPDATE " + ClipHisotryEntry.ClipEntry.TABLE_NAME +" SET " +
                                ClipHisotryEntry.ClipEntry.COLUMN_NAME_UPDATE_TIME + " = CURRENT_TIMESTAMP " +
                                "WHERE "+ ClipHisotryEntry.ClipEntry._ID + " = " + String.valueOf(id);
                        db.execSQL(query);
                        sendMessage(getString(R.string.action_db_updated),"id",id);
                    }
                }
                else {
                    ContentValues values = new ContentValues();
                    values.put(ClipHisotryEntry.ClipEntry.COLUMN_NAME_DATA,text);
                    long id = db.insert(ClipHisotryEntry.ClipEntry.TABLE_NAME, null, values);
                    sendMessage(getString(R.string.action_db_inserted),"id",id);
                }
                cursor.close();
            }
        }
    };
    private BroadcastReceiver localMsgReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(context.getString(R.string.action_db_inserted))){
                //madapter.refreshClipHistory(db);
            }
            else if (action.equals(context.getString(R.string.action_db_updated))){
                //madapter.refreshClipHistory(db);
            }
            else if (action.equals(context.getString(R.string.action_db_deleted))){
                //madapter.refreshClipHistory(db);
            }
            else if (action.equals(context.getString(R.string.action_db_delete))){
                //do delete
                String id = intent.getStringExtra("id");
                if (id != null){
                    String selection = ClipHisotryEntry.ClipEntry._ID +" = ?";
                    String[] selectionArgs = {String.valueOf(id)};
                    db.delete(ClipHisotryEntry.ClipEntry.TABLE_NAME,selection,selectionArgs);
                    sendMessage(getString(R.string.action_db_deleted),"id",id);
                }
            }
            else if (action.equals(getString(R.string.action_fab_toggle))){
                toggleFab();
            }
            else if (action.equals(getString(R.string.action_fab_refresh))){
                refreshFAB();
            }
            else if (action.equals(getString(R.string.action_db_clear))){
                db.delete(ClipHisotryEntry.ClipEntry.TABLE_NAME,null,null);
                sendMessage(getString(R.string.action_db_cleared),null,null);
            }
        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        fab_status = false;
        boolean fab_status = sharedPreferences.getBoolean(getString(R.string.preference_fab_switch_key),false);
        if (fab_status){
            toggleFab();
        }

        mDbHelper = new ClipHistoryDbHelper(this);
        db = mDbHelper.getWritableDatabase();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getString(R.string.action_db_inserted));
        intentFilter.addAction(getString(R.string.action_db_updated));
        intentFilter.addAction(getString(R.string.action_db_deleted));
        intentFilter.addAction(getString(R.string.action_db_delete));
        intentFilter.addAction(getString(R.string.action_db_clear));
        intentFilter.addAction(getString(R.string.action_fab_refresh));
        intentFilter.addAction(getString(R.string.action_fab_toggle));
        LocalBroadcastManager.getInstance(this).registerReceiver(localMsgReceiver,intentFilter);

        mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);


        Intent notificationIntent = new Intent(this,HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,0);
        Notification notification = new Notification.Builder(this)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_msg))
                .setSmallIcon(R.drawable.ic_assignment_turned_in_black_36dp)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_MIN)
                .build();
        startForeground(NOTIFICATION_ID,notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mDbHelper.close();
        if (fab_status){
            toggleFab();
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localMsgReceiver);
        mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
        stopSelf();
    }


    private void sendMessage(String action, String key, long value){
        sendMessage(action,key,String.valueOf(value));
    }
    private void sendMessage(String action, String key, String value) {
        Log.d("sender", "Broadcasting message");
        Intent intent = new Intent(action);
        intent.putExtra(key, value);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public static float convertDpToPixel(float dp, WindowManager windowManager) {
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public static float convertPixelsToDp(float px, WindowManager windowManager) {
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    public void toggleFab(){
        if (!fab_status){
            wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            overlayView = new OverlayView(this);

            overlayView.setFabOnTouchListener(new View.OnTouchListener() {
                int x, y;
                float touchedX, touchedY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    WindowManager.LayoutParams updatedParameters = parameters;
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            x = updatedParameters.x;
                            y = updatedParameters.y;
                            touchedX = event.getRawX();
                            touchedY = event.getRawY();
                            break;
                        case MotionEvent.ACTION_UP:
                            if (Math.abs(touchedX - event.getRawX()) < 10
                                    && Math.abs(touchedY - event.getRawY()) < 10) {
                                overlayView.Toggle();
                                showPopup(overlayView);
                            }
                        case MotionEvent.ACTION_MOVE:
                            updatedParameters.x=(int)(x+(event.getRawX()-touchedX));
                            updatedParameters.y = (int) (y + (event.getRawY() - touchedY));
                            wm.updateViewLayout(overlayView, updatedParameters);
                            break;
                        default:
                            break;
                    }
                    return false;
                }
            });

            parameters = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSPARENT);
            Point size = new Point();
            wm.getDefaultDisplay().getSize(size);
            parameters.x = size.x - (int) convertDpToPixel(100, wm);
            parameters.y = size.y / 3;
            parameters.gravity = Gravity.TOP | Gravity.LEFT;

            wm.addView(overlayView,parameters);
            fab_status = true;
        }
        else {
            wm.removeView(overlayView);
            fab_status = false;
        }
    }

    public void refreshFAB(){
        if (fab_status){
            toggleFab();
            toggleFab();
        }
    }

    public void showPopup(View v){

    }
}
