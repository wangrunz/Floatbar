package runzhong.floatbar;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.view.ContextThemeWrapper;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by wrz19 on 3/28/2017.
 */

public class OverlayView extends CoordinatorLayout {
    private FloatingActionButton fab;
    private int fabColor;
    private float alpha;
    public OverlayView(Context context) {
        super(new ContextThemeWrapper(context, R.style.Theme_AppCompat_Light));
        init();
    }

    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init(){
        inflate(getContext(),R.layout.overlay_view,this);
        fab = (FloatingActionButton)findViewById(R.id.overlay_fab);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String fab_color = sharedPreferences.getString(getContext().getString(R.string.preference_fab_color_key),getContext().getString(R.string.preference_fab_color_default));
        fabColor = Color.parseColor(fab_color);
        String fab_alpha = sharedPreferences.getString(getContext().getString(R.string.preference_fab_alpha_key),"100");
        alpha = Integer.valueOf(fab_alpha)/100f;
        String fab_size = sharedPreferences.getString(getContext().getString(R.string.preference_fab_size_key),"mini");
        if (fab_size.equals("mini")){
            fab.setSize(FloatingActionButton.SIZE_MINI);
        }
        else {
            fab.setSize(FloatingActionButton.SIZE_NORMAL);
        }

        fab.setBackgroundTintList(ColorStateList.valueOf(fabColor));
        fab.setAlpha(alpha);
    }

    public void setFabOnTouchListener(OnTouchListener listener){
        fab.setOnTouchListener(listener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        switch (event.getAction()){
            case MotionEvent.ACTION_OUTSIDE:
                if (fab.getVisibility()==GONE){
                    //Toggle();
                }
            default:
                break;
        }
        return false;
    }

    public void Toggle() {
        //Do hide list and animations
        if (fab.getVisibility()==VISIBLE){
            fab.setVisibility(GONE);
        }
        else {
            fab.setVisibility(VISIBLE);
        }
    }


}
