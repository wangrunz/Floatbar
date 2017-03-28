package runzhong.floatbar;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by George on 11/9/2016.
 */

public class FloatingRecyclerAdapter extends RecyclerView.Adapter {
    private List<HashMap> mClipHistoryArrary = new Vector<>();

    public void refreshClipHistory(SQLiteDatabase db){
        String[] projection = {
                ClipHisotryEntry.ClipEntry._ID,
                ClipHisotryEntry.ClipEntry.COLUMN_NAME_DATA,
                ClipHisotryEntry.ClipEntry.COLUMN_NAME_UPDATE_TIME
        };
        String sortOrder = ClipHisotryEntry.ClipEntry.COLUMN_NAME_UPDATE_TIME+ " DESC";
        Cursor cursor = db.query(ClipHisotryEntry.ClipEntry.TABLE_NAME, projection, null, null, null, null, sortOrder);
        List<HashMap> itemLists = new Vector<>();
        while (cursor.moveToNext()){
            HashMap map = new HashMap();
            long id = cursor.getLong(cursor.getColumnIndex(ClipHisotryEntry.ClipEntry._ID));
            String data = cursor.getString(cursor.getColumnIndex(ClipHisotryEntry.ClipEntry.COLUMN_NAME_DATA));
            Timestamp update_time = Timestamp.valueOf(cursor.getString(cursor.getColumnIndex(ClipHisotryEntry.ClipEntry.COLUMN_NAME_UPDATE_TIME)));
            map.put("id",id);
            map.put("data",data);
            map.put("update_time",update_time);
            itemLists.add(map);
        }
        mClipHistoryArrary=itemLists;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            default:
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.clip_cell_mini, parent, false);
                DefaultClipDataViewHolder vh = new DefaultClipDataViewHolder(v);
                vh.textView = (TextView) v.findViewById(R.id.clipdatatext);
                return vh;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final DefaultClipDataViewHolder vh = (DefaultClipDataViewHolder) holder;
        vh.map = mClipHistoryArrary.get(position);
        vh.textView.setText(String.valueOf(vh.map.get("data")));
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager mClipboardManager = (ClipboardManager) vh.itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                mClipboardManager.setPrimaryClip(ClipData.newPlainText(null,String.valueOf(vh.map.get("data"))));
            }
        });
        vh.itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mClipHistoryArrary.size();
    }

    public static class DefaultClipDataViewHolder extends RecyclerView.ViewHolder {
        public View itemView;
        public TextView textView;
        public ClipData data;
        public HashMap map;

        public DefaultClipDataViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }
    }
}
