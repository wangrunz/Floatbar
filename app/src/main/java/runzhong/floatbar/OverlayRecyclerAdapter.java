package runzhong.floatbar;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
/**
 * Created by George on 11/9/2016.
 */

public class OverlayRecyclerAdapter extends RecyclerView.Adapter {
    List<HashMap> ClipMenu = new Vector<>();

    public void refresh(List<HashMap> dbdata){
        ClipMenu.clear();
        notifyDataSetChanged();
        Collections.reverse(dbdata);
        for (HashMap data: dbdata){
            ClipMenu.add(0,data);
            notifyItemInserted(0);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            default:
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.overlay_list_item, parent, false);
                OverLayRecyclerItemHolder itemHolder = new OverLayRecyclerItemHolder(v);
                return itemHolder;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()){
            case 0:
                OverLayRecyclerItemHolder itemHolder = (OverLayRecyclerItemHolder)holder;
                final String text = String.valueOf(ClipMenu.get(position).get("text"));
                itemHolder.textView.setText(text);
                itemHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager mClipboardManager = (ClipboardManager) holder.itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        mClipboardManager.setPrimaryClip(ClipData.newPlainText(null,text));
                    }
                });
                break;
        }
    }

    @Override
    public int getItemCount() {
        return ClipMenu.size();
    }

    @Override
    public int getItemViewType(int position){
        int viewType;
        switch (String.valueOf(ClipMenu.get(position).get("type"))){
            default:
                viewType = 0;
        }
        return viewType;
    }

    public class OverLayRecyclerItemHolder extends RecyclerView.ViewHolder{
        private ImageView imageView;
        private TextView textView;
        public OverLayRecyclerItemHolder(View view){
            super(view);
            textView = (TextView)view.findViewById(R.id.overlayItemText);
            imageView = (ImageView)view.findViewById(R.id.overlayItemIcon);
        }
    }
}
