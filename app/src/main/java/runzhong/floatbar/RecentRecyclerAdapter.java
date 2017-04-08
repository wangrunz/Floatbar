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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by wrz19 on 3/28/2017.
 */

public class RecentRecyclerAdapter extends RecyclerView.Adapter {
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
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.clip_cell_normal, parent, false);
                RecentRecyclerAdapter.DefaultClipDataViewHolder vh = new RecentRecyclerAdapter.DefaultClipDataViewHolder(v);
                return vh;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final RecentRecyclerAdapter.DefaultClipDataViewHolder vh = (RecentRecyclerAdapter.DefaultClipDataViewHolder) holder;
        vh.map = mClipHistoryArrary.get(position);
        final String text = String.valueOf(vh.map.get("data"));
        vh.text.setText(text);
        vh.title.setText(String.valueOf(vh.map.get("title")));

        vh.subtitle.setText(new SimpleDateFormat("yyyy.MM.dd HH:mm").format(Timestamp.valueOf(String.valueOf(vh.map.get("update_time")))));

        vh.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId()==vh.title.getId()){
                    vh.title.setCursorVisible(true);
                }
            }
        });
        vh.title.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE){
                    String action = vh.itemView.getContext().getString(R.string.action_db_title_changed);
                    Intent intent = new Intent(action);
                    Bundle extras = new Bundle();
                    extras.putString("title", v.getText().toString());
                    extras.putString("id",String.valueOf(vh.map.get("id")));
                    intent.putExtras(extras);
                    LocalBroadcastManager.getInstance(vh.itemView.getContext()).sendBroadcast(intent);
                    v.setCursorVisible(false);
                    return false;
                }
                return false;
            }
        });

        if (vh.map.get("favorite").equals(1)){
            vh.favoritebutton.setTextColor(vh.itemView.getResources().getColorStateList(R.color.reversetextselector,null));
        }
        else {
            vh.favoritebutton.setTextColor(vh.itemView.getResources().getColorStateList(R.color.textselector,null));
        }

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

        vh.copybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager mClipboardManager = (ClipboardManager) vh.itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                mClipboardManager.setPrimaryClip(ClipData.newPlainText(null,text));
            }
        });

        vh.sharebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT,text);
                shareIntent.setType("text/plain");
                vh.itemView.getContext().startActivity(shareIntent);
            }
        });

        vh.deletebutton.setOnClickListener(new View.OnClickListener() {
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
        public EditText title;
        public TextView subtitle;
        public TextView text;
        public Button sharebutton;
        public Button deletebutton;
        public Button copybutton;
        public Button favoritebutton;
        public HashMap map;

        public DefaultClipDataViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            title = (EditText) itemView.findViewById(R.id.Title);
            subtitle = (TextView) itemView.findViewById(R.id.Subtitle);
            text = (TextView) itemView.findViewById(R.id.clipdatatext);
            favoritebutton = (Button) itemView.findViewById(R.id.favoritebutton);
            sharebutton = (Button) itemView.findViewById(R.id.sharebutton);
            deletebutton = (Button) itemView.findViewById(R.id.deletebutton);
            copybutton = (Button) itemView.findViewById(R.id.copybutton);

        }
    }
}
