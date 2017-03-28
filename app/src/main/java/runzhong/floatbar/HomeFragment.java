package runzhong.floatbar;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ChangeFragmentCallbacks mCallbacks;

    public interface ChangeFragmentCallbacks{
        public void ChangeFragment(int id);
    }

    private RecyclerView recyclerView;
    private TextView noticeText;
    private HomeRecyclerAdapter adapter;

    private ClipHistoryDbHelper mDbHelper;
    private SQLiteDatabase db;

    private Context context;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        context = view.getContext();
        recyclerView = (RecyclerView) (view.findViewById(R.id.home_recycler));
        noticeText = (TextView) (view.findViewById(R.id.noticeText));
        mCallbacks = (ChangeFragmentCallbacks) context;
        noticeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbacks.ChangeFragment(R.id.navigation_setting);
            }
        });

        adapter = new HomeRecyclerAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
        mDbHelper = new ClipHistoryDbHelper(context);
        db = mDbHelper.getReadableDatabase();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getString(R.string.action_db_inserted));
        intentFilter.addAction(getString(R.string.action_db_updated));
        intentFilter.addAction(getString(R.string.action_db_deleted));
        LocalBroadcastManager.getInstance(context).registerReceiver(localMsgReceiver,intentFilter);
        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        adapter.refreshClipHistory(db);
        if (isMyServiceRunning(FloatingWindowService.class)){
            noticeText.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        else {
            noticeText.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mDbHelper.close();
        LocalBroadcastManager.getInstance(context).unregisterReceiver(localMsgReceiver);
    }

    private BroadcastReceiver localMsgReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(context.getString(R.string.action_db_inserted))){
                adapter.refreshClipHistory(db);
            }
            else if (action.equals(context.getString(R.string.action_db_updated))){
                adapter.refreshClipHistory(db);
            }
            else if (action.equals(context.getString(R.string.action_db_deleted))){
                adapter.refreshClipHistory(db);
            }
        }
    };


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
