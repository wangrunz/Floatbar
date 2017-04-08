package runzhong.floatbar;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
/**
 * Created by George on 11/9/2016.
 */

public class OverlayRecyclerAdapter extends RecyclerView.Adapter {
    protected List<HashMap> mClipHistoryArrary = new Vector<>();

    public void refreshClipHistory(SQLiteDatabase db){
        String[] projection = {
                ClipHistoryEntry.ClipEntry._ID,
                ClipHistoryEntry.ClipEntry.COLUMN_NAME_DATA,
                ClipHistoryEntry.ClipEntry.COLUMN_NAME_UPDATE_TIME,
                ClipHistoryEntry.ClipEntry.COLUMN_NAME_TITLE,
                ClipHistoryEntry.ClipEntry.COLUMN_NAME_FAVORITE
        };
        String sortOrder = ClipHistoryEntry.ClipEntry.COLUMN_NAME_UPDATE_TIME+ " DESC";
        Cursor cursor = db.query(ClipHistoryEntry.ClipEntry.TABLE_NAME, projection, null, null, null, null, sortOrder);
        List<HashMap> itemLists = new Vector<>();
        while (cursor.moveToNext()){
            HashMap map = new HashMap();
            long id = cursor.getLong(cursor.getColumnIndex(ClipHistoryEntry.ClipEntry._ID));
            String data = cursor.getString(cursor.getColumnIndex(ClipHistoryEntry.ClipEntry.COLUMN_NAME_DATA));
            String title = cursor.getString(cursor.getColumnIndex(ClipHistoryEntry.ClipEntry.COLUMN_NAME_TITLE));
            int favorite = cursor.getInt(cursor.getColumnIndex(ClipHistoryEntry.ClipEntry.COLUMN_NAME_FAVORITE));
            Timestamp update_time = Timestamp.valueOf(cursor.getString(cursor.getColumnIndex(ClipHistoryEntry.ClipEntry.COLUMN_NAME_UPDATE_TIME)));
            map.put("id",id);
            map.put("data",data);
            map.put("title",title);
            map.put("update_time",update_time);
            map.put("favorite",favorite);
            itemLists.add(map);
        }
        mClipHistoryArrary=itemLists;
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            default:
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.overlay_list_item, parent, false);
                OverlayRecyclerAdapter.DefaultClipDataViewHolder vh = new OverlayRecyclerAdapter.DefaultClipDataViewHolder(v);
                return vh;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final OverlayRecyclerAdapter.DefaultClipDataViewHolder vh = (OverlayRecyclerAdapter.DefaultClipDataViewHolder) holder;
        vh.map = mClipHistoryArrary.get(position);
        final String text = String.valueOf(vh.map.get("data"));
        vh.text.setText(text);
        vh.title.setText(String.valueOf(vh.map.get("title")));
        vh.subtitle.setText(new SimpleDateFormat("yyyy.MM.dd HH:mm").format(Timestamp.valueOf(String.valueOf(vh.map.get("update_time")))));
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager mClipboardManager = (ClipboardManager) vh.itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                mClipboardManager.setPrimaryClip(ClipData.newPlainText(null,text));
                Intent intent = new Intent(vh.itemView.getResources().getString(R.string.action_fab_hide_toggle));
                LocalBroadcastManager.getInstance(vh.itemView.getContext()).sendBroadcast(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return mClipHistoryArrary.size();
    }

    public static class DefaultClipDataViewHolder extends RecyclerView.ViewHolder {
        public View itemView;
        public TextView title;
        public TextView subtitle;
        public TextView text;
        public HashMap map;

        public DefaultClipDataViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            title = (TextView) itemView.findViewById(R.id.Title);
            subtitle = (TextView) itemView.findViewById(R.id.Subtitle);
            text = (TextView) itemView.findViewById(R.id.clipdatatext);
        }
    }
}
