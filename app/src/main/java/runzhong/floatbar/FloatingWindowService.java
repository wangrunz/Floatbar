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
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class FloatingWindowService extends Service {
    private static final String TAG = "ClipboardManager";
    private ClipHistoryDbHelper mDbHelper;
    private SQLiteDatabase db;
    private static final int NOTIFICATION_ID = 100;
    private WindowManager wm;
    private WindowManager.LayoutParams parameters;
    private LinearLayout ll;
    private RecyclerView rv;
    private FloatingRecyclerAdapter madapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private FloatingActionButton fab;
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
                madapter.refreshClipHistory(db);
            }
            else if (action.equals(context.getString(R.string.action_db_updated))){
                madapter.refreshClipHistory(db);
            }
            else if (action.equals(context.getString(R.string.action_db_deleted))){
                madapter.refreshClipHistory(db);
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
        }
    };

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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getString(R.string.action_db_inserted));
        intentFilter.addAction(getString(R.string.action_db_updated));
        intentFilter.addAction(getString(R.string.action_db_deleted));
        intentFilter.addAction(getString(R.string.action_db_delete));
        LocalBroadcastManager.getInstance(this).registerReceiver(localMsgReceiver,intentFilter);

        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        ll = new LinearLayout(this);

        mDbHelper = new ClipHistoryDbHelper(this);
        db = mDbHelper.getWritableDatabase();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String fab_color = sharedPreferences.getString(getString(R.string.preference_fab_color_key),getString(R.string.preference_fab_color_default));
        int fabColorCode = Color.parseColor(fab_color);
        String rv_color = sharedPreferences.getString(getString(R.string.preference_rv_color_key),getString(R.string.preference_rv_color_default));
        String fab_side = sharedPreferences.getString(getString(R.string.preference_side_key),getString(R.string.preference_side_default));
        String rv_alpha = sharedPreferences.getString(getString(R.string.preference_rv_alpha_key),"50");

        fab = new FloatingActionButton(new android.view.ContextThemeWrapper(this, R.style.AppTheme));
        fab.setLayoutParams(new ViewGroup.LayoutParams((int) convertDpToPixel(40, wm), (int) convertDpToPixel(40, wm)));
        fab.setUseCompatPadding(true);
        fab.setClickable(true);
        fab.setImageResource(R.drawable.ic_assignment_black_36dp);
        fab.setCompatElevation(convertDpToPixel(6, wm));
        fab.setSize(fab.SIZE_MINI);
        fab.setBackgroundTintList(ColorStateList.valueOf(fabColorCode));


        rv = new RecyclerView(this);
        rv.setVerticalScrollBarEnabled(true);
        RecyclerView.LayoutParams rvParameters = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT);
        rv.setLayoutParams(rvParameters);
        mLayoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(mLayoutManager);
        rv.setBackgroundColor(Color.parseColor("#"+ String.format("%02X",(int)(Integer.parseInt(rv_alpha)*2.55))+rv_color.substring(1,7)));
        madapter = new FloatingRecyclerAdapter();
        rv.setAdapter(madapter);
        rv.setVisibility(View.GONE);

        LinearLayout.LayoutParams llParameters = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        ll.setBackgroundColor(Color.argb(0, 255, 255, 255));
        ll.setLayoutParams(llParameters);
        if (fab_side.equals(getString(R.string.preference_side_default))){
            ll.setGravity(Gravity.TOP | Gravity.LEFT);
        }
        else {
            ll.setGravity(Gravity.TOP | Gravity.RIGHT);
        }
        ll.setOrientation(ll.VERTICAL);

        parameters = new WindowManager.LayoutParams((int) convertDpToPixel(60, wm), (int) convertDpToPixel(60, wm), WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        parameters.x = size.x - (int) convertDpToPixel(50, wm);
        parameters.y = size.y / 3;
        if (fab_side.equals(getString(R.string.preference_side_default))){
            parameters.gravity = Gravity.TOP | Gravity.LEFT;
        }
        else {
            parameters.gravity = Gravity.TOP | Gravity.RIGHT;
        }


        ll.addView(fab);
        ll.addView(rv);
        wm.addView(ll, parameters);

        fab.setOnTouchListener(new View.OnTouchListener() {

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
                        if (Math.abs(touchedX - event.getRawX()) < 10 && Math.abs(touchedY - event.getRawY()) < 10) {
                            if (rv.getVisibility() == View.VISIBLE) {
                                rv.setVisibility(View.GONE);
                                updatedParameters.width = (int) convertDpToPixel(60, wm);
                                updatedParameters.height = (int) convertDpToPixel(60, wm);
                            } else {
                                updatedParameters.width = (int) convertDpToPixel(150, wm);
                                updatedParameters.height = (int) convertDpToPixel(300, wm);
                                rv.setVisibility(View.VISIBLE);
                            }
                            wm.updateViewLayout(ll, updatedParameters);
                        }
                    case MotionEvent.ACTION_MOVE:
                        //updatedParameters.x=(int)(x+(event.getRawX()-touchedX));
                        updatedParameters.y = (int) (y + (event.getRawY() - touchedY));

                        wm.updateViewLayout(ll, updatedParameters);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

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

        madapter.refreshClipHistory(db);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        wm.removeView(ll);
        mDbHelper.close();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localMsgReceiver);
        mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
        stopSelf();
    }
}
