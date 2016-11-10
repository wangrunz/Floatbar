package runzhong.floatbar;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.Image;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Vector;


public class FloatingWindow extends Service{
    private WindowManager wm;
    private WindowManager.LayoutParams parameters;
    private LinearLayout ll;
    private RecyclerView rv;
    private RecyclerView.Adapter madapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private FloatingActionButton fab;
    private static final String TAG = "ClipboardManager";
    private ClipboardManager mClipboardManager;
    private ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener=new ClipboardManager.OnPrimaryClipChangedListener() {
        @Override
        public void onPrimaryClipChanged() {
            ClipData newClip = mClipboardManager.getPrimaryClip();
            ((mAdapter)rv.getAdapter()).mClipHistory.add(0,newClip);
            (rv.getAdapter()).notifyItemInserted(0);
            mLayoutManager.scrollToPosition(0);
            Log.d(TAG,newClip.toString());

        }
    };
    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(float dp, WindowManager windowManager){
        DisplayMetrics metrics=new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, WindowManager windowManager){
        DisplayMetrics metrics=new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        float dp = px / ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onCreate(){
        super.onCreate();

        wm =(WindowManager)getSystemService(WINDOW_SERVICE);
        ll = new LinearLayout(this);


        fab=new FloatingActionButton(new android.view.ContextThemeWrapper(this, R.style.AppTheme));
        fab.setLayoutParams(new ViewGroup.LayoutParams((int)convertDpToPixel(40,wm),(int)convertDpToPixel(40,wm)));
        fab.setUseCompatPadding(true);
        fab.setClickable(true);
        fab.setImageResource(R.drawable.ic_list);
        fab.setCompatElevation(convertDpToPixel(6,wm));
        fab.setSize(fab.SIZE_MINI);


        rv = new RecyclerView(this);
        rv.setVerticalScrollBarEnabled(true);
        RecyclerView.LayoutParams rvParameters = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,RecyclerView.LayoutParams.MATCH_PARENT);
        rv.setLayoutParams(rvParameters);
        mLayoutManager=new LinearLayoutManager(this);
        rv.setLayoutManager(mLayoutManager);
        rv.setBackgroundColor(Color.argb(20,255,255,255));
        madapter=new mAdapter();
        rv.setAdapter(madapter);

        LinearLayout.LayoutParams llParameters = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        ll.setBackgroundColor(Color.argb(0,255,255,255));
        ll.setLayoutParams(llParameters);
        ll.setGravity(Gravity.TOP);
        ll.setOrientation(ll.VERTICAL);

        parameters = new WindowManager.LayoutParams((int)convertDpToPixel(150,wm),(int)convertDpToPixel(300,wm),WindowManager.LayoutParams.TYPE_PHONE,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        parameters.x=size.x-(int)convertDpToPixel(50,wm);
        parameters.y=size.y/3;
        parameters.gravity= Gravity.TOP|Gravity.LEFT;

        ll.addView(fab);
        ll.addView(rv);
        wm.addView(ll,parameters);

        fab.setOnTouchListener(new View.OnTouchListener() {
            private WindowManager.LayoutParams updatedParameters = parameters;
            int x,y;
            float touchedX, touchedY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x = updatedParameters.x;
                        y = updatedParameters.y;
                        touchedX=event.getRawX();
                        touchedY=event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        updatedParameters.x=(int)(x+(event.getRawX()-touchedX));
                        updatedParameters.y=(int)(y+(event.getRawY()-touchedY));

                        wm.updateViewLayout(ll,updatedParameters);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        fab.setOnClickListener(new View.OnClickListener() {
            private WindowManager.LayoutParams updatedParameters = parameters;
            @Override
            public void onClick(View v) {
                if (rv.getVisibility() == View.VISIBLE) {
                    rv.setVisibility(View.GONE);
                    updatedParameters.width=(int)convertDpToPixel(80,wm);
                    updatedParameters.height=(int)convertDpToPixel(80,wm);
                } else {
                    updatedParameters.width=(int)convertDpToPixel(150,wm);
                    updatedParameters.height=(int)convertDpToPixel(300,wm);
                    rv.setVisibility(View.VISIBLE);
                }
                wm.updateViewLayout(ll,updatedParameters);
            }
        });
        mClipboardManager=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);


    }
    @Override
    public void onDestroy(){
        wm.removeView(ll);
        stopSelf();
    }
}
