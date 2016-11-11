package runzhong.floatbar;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Vector;

/**
 * Created by George on 11/9/2016.
 */

public class mAdapter extends RecyclerView.Adapter {
    public List<ClipData> mClipHistory= new Vector<>();

    public static class DefaultClipDataViewHolder extends RecyclerView.ViewHolder{
        public View itemView;
        public TextView textView;
        public ClipData data;
        public DefaultClipDataViewHolder(View itemView){
            super(itemView);
            this.itemView=itemView;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            default:
                View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.default_clipdata,parent,false);
                DefaultClipDataViewHolder vh = new DefaultClipDataViewHolder(v);
                vh.textView=(TextView)v.findViewById(R.id.clipdatatext);
                return vh;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final DefaultClipDataViewHolder vh = (DefaultClipDataViewHolder)holder;
        vh.textView.setText(mClipHistory.get(position).getItemAt(0).getText());
        vh.data=mClipHistory.get(position);
        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager mClipboardManager = (ClipboardManager)vh.itemView.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                int itemIndex=mClipHistory.indexOf(vh.data);
                mClipHistory.remove(itemIndex);
                notifyItemRemoved(itemIndex);
                mClipboardManager.setPrimaryClip(vh.data);
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
        return mClipHistory.size();
    }
}
