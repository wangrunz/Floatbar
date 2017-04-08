package runzhong.floatbar;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by wrz19 on 4/1/2017.
 */

public class FavoriteRecyclerAdapter extends RecentRecyclerAdapter {
    @Override
    public void refreshClipHistory(SQLiteDatabase db){
        String[] projection = {
                ClipHistoryEntry.ClipEntry._ID,
                ClipHistoryEntry.ClipEntry.COLUMN_NAME_DATA,
                ClipHistoryEntry.ClipEntry.COLUMN_NAME_UPDATE_TIME,
                ClipHistoryEntry.ClipEntry.COLUMN_NAME_TITLE,
                ClipHistoryEntry.ClipEntry.COLUMN_NAME_FAVORITE
        };
        String selection = ClipHistoryEntry.ClipEntry.COLUMN_NAME_FAVORITE + " = ?";
        String[] selectionArgs = {"1"};
        String sortOrder = ClipHistoryEntry.ClipEntry.COLUMN_NAME_UPDATE_TIME+ " DESC";
        Cursor cursor = db.query(ClipHistoryEntry.ClipEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position){
        super.onBindViewHolder(holder,position);
        final RecentRecyclerAdapter.DefaultClipDataViewHolder vh = (RecentRecyclerAdapter.DefaultClipDataViewHolder) holder;

        vh.favoritebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (String.valueOf(vh.map.get("favorite"))=="1"){
                    vh.favoritebutton.setTextColor(vh.itemView.getResources().getColorStateList(R.color.textselector,null));
                    vh.map.put("favorite","0");
                    String action = vh.itemView.getContext().getString(R.string.action_db_favorite_changed);
                    Intent intent = new Intent(action);
                    Bundle extras = new Bundle();
                    extras.putString("favorite", "0");
                    extras.putString("id",String.valueOf(vh.map.get("id")));
                    intent.putExtras(extras);
                    LocalBroadcastManager.getInstance(vh.itemView.getContext()).sendBroadcast(intent);
                    mClipHistoryArrary.remove(position);
                    notifyItemRemoved(position);
                }
                else {
                    vh.favoritebutton.setTextColor(vh.itemView.getResources().getColorStateList(R.color.reversetextselector,null));
                    vh.map.put("favorite","1");
                    String action = vh.itemView.getContext().getString(R.string.action_db_favorite_changed);
                    Intent intent = new Intent(action);
                    Bundle extras = new Bundle();
                    extras.putString("favorite", "1");
                    extras.putString("id",String.valueOf(vh.map.get("id")));
                    intent.putExtras(extras);
                    LocalBroadcastManager.getInstance(vh.itemView.getContext()).sendBroadcast(intent);
                }

            }
        });
    }
}
