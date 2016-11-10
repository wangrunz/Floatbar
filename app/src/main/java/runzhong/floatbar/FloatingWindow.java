package runzhong.floatbar;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.media.Image;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
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
    private GridLayout gl;
    private ImageButton ib;
    private TextView tv;
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


            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(metrics);
            if(metrics.heightPixels>400)
            {
                parameters.height=400;
                wm.updateViewLayout(ll,parameters);
            }
        }
    };
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
        ib = new ImageButton(this);
        ib.setBackgroundColor(Color.argb(0,255,255,255));


        ib.setImageResource(R.drawable.ic_list);
        ib.setLayoutParams(new ViewGroup.LayoutParams(80,80));
        ib.setScaleType(ImageView.ScaleType.FIT_CENTER);
        gl = new GridLayout(this);
        gl.setColumnCount(2);
        gl.setRowCount(1);
        gl.setBackgroundColor(Color.argb(60,255,255,255));


        tv = new TextView(this);
        tv.setText("Clipboard");
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(Color.argb(100,0,0,0));
        tv.setHeight(80);
        tv.setWidth(160);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rv.getVisibility() == View.VISIBLE) {
                    rv.setVisibility(View.GONE);
                } else {
                    rv.setVisibility(View.VISIBLE);
                }
            }
        });
        gl.addView(ib);
        gl.addView(tv);


        rv = new RecyclerView(this);
        rv.setVerticalScrollBarEnabled(true);
        RecyclerView.LayoutParams rvParameters = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,RecyclerView.LayoutParams.MATCH_PARENT);
        rv.setLayoutParams(rvParameters);
        mLayoutManager=new LinearLayoutManager(this);
        rv.setLayoutManager(mLayoutManager);
        madapter=new mAdapter();
        rv.setAdapter(madapter);

        LinearLayout.LayoutParams llParameters = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        ll.setBackgroundColor(Color.argb(0,255,255,255));
        ll.setLayoutParams(llParameters);
        ll.setGravity(Gravity.TOP);
        ll.setOrientation(ll.VERTICAL);

        parameters = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.TYPE_PHONE,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        parameters.x=size.x/2;
        parameters.y=size.y/3;
        parameters.gravity= Gravity.TOP;

        ll.addView(gl);
        ll.addView(rv);
        wm.addView(ll,parameters);
        ll.setOnTouchListener(new View.OnTouchListener(){
            private WindowManager.LayoutParams updatedParameters = parameters;
            int x,y;
            float touchedX, touchedY;

            @Override
            public boolean onTouch(View arg0, MotionEvent event){
                switch (event.getAction()){
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


        mClipboardManager=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);


    }
    @Override
    public void onDestroy(){
        wm.removeView(ll);
        stopSelf();
    }
}
