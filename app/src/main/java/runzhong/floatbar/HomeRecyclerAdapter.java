package runzhong.floatbar;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by wrz19 on 3/28/2017.
 */

public class HomeRecyclerAdapter extends RecyclerView.Adapter {
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
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.clip_cell_normal, parent, false);
                HomeRecyclerAdapter.DefaultClipDataViewHolder vh = new HomeRecyclerAdapter.DefaultClipDataViewHolder(v);
                return vh;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final HomeRecyclerAdapter.DefaultClipDataViewHolder vh = (HomeRecyclerAdapter.DefaultClipDataViewHolder) holder;
        vh.map = mClipHistoryArrary.get(position);
        final String text = String.valueOf(vh.map.get("data"));
        vh.textView.setText(text);

        vh.copyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager mClipboardManager = (ClipboardManager) vh.itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                mClipboardManager.setPrimaryClip(ClipData.newPlainText(null,text));
            }
        });

        vh.shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT,text);
                shareIntent.setType("text/plain");
                vh.itemView.getContext().startActivity(shareIntent);
            }
        });

        vh.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                String action = vh.itemView.getContext().getString(R.string.action_db_delete);
                                Intent intent = new Intent(action);
                                intent.putExtra("id", String.valueOf(vh.map.get("id")));
                                LocalBroadcastManager.getInstance(vh.itemView.getContext()).sendBroadcast(intent);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage(R.string.preference_reset_msg)
                        .setCancelable(true)
                        .setNegativeButton(R.string.clipdata_delete_btn_no, listener)
                        .setPositiveButton(R.string.clipdata_delete_btn_yes, listener)
                        .show();
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
        public ImageButton copyButton;
        public ImageButton deleteButton;
        public ImageButton shareButton;
        public HashMap map;

        public DefaultClipDataViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            textView = (TextView) itemView.findViewById(R.id.clipdatatext);
            copyButton = (ImageButton) itemView.findViewById(R.id.copyButton);
            deleteButton = (ImageButton) itemView.findViewById(R.id.deleteButton);
            shareButton = (ImageButton) itemView.findViewById(R.id.shareButton);
        }
    }
}
