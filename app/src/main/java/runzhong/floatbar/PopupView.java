package runzhong.floatbar;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by wrz19 on 4/8/2017.
 */

public class PopupView extends ConstraintLayout {
    private RecyclerView recyclerView;
    private OverlayRecyclerAdapter overlayRecyclerAdapter;
    private ClipHistoryDbHelper mDbHelper;
    private SQLiteDatabase db;
    public PopupView(Context context) {
        super(context);
        init();
    }
    private void init(){
        inflate(getContext(),R.layout.popup_view,this);
        recyclerView = (RecyclerView)findViewById(R.id.popup_recycler);
        overlayRecyclerAdapter = new OverlayRecyclerAdapter();
        recyclerView.setAdapter(overlayRecyclerAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mDbHelper = new ClipHistoryDbHelper(getContext());
        db = mDbHelper.getReadableDatabase();
        overlayRecyclerAdapter.refreshClipHistory(db);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction()==MotionEvent.ACTION_OUTSIDE){
            Intent intent = new Intent(getResources().getString(R.string.action_fab_hide_toggle));
            LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
            return false;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mDbHelper.close();
    }
}
