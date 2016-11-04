package runzhong.floatbar;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


public class FloatingWindow extends Service{

    private WindowManager wm;
    private LinearLayout ll;
    private TextView tv;

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
        tv = new TextView(this);
        tv.setText("X");
        tv.setGravity(Gravity.CENTER);
        tv.setTextColor(Color.argb(100,0,0,0));

        LinearLayout.LayoutParams llParameters = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        ll.setBackgroundColor(Color.argb(66,255,255,255));
        ll.setLayoutParams(llParameters);
        ll.setGravity(Gravity.CENTER);

        final WindowManager.LayoutParams parameters = new WindowManager.LayoutParams(100,100,WindowManager.LayoutParams.TYPE_PHONE,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSPARENT);
        Point size = new Point();
        wm.getDefaultDisplay().getSize(size);
        parameters.x=0;
        parameters.y=0;
        parameters.gravity= Gravity.CENTER;

        ll.addView(tv);
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
    }
    @Override
    public void onDestroy(){
        wm.removeView(ll);
        stopSelf();
    }
}
