package runzhong.floatbar;

import android.app.ActivityManager;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class HomeActivity extends AppCompatActivity implements SettingFragment.ReloadCallbacks,RecentFragment.ChangeFragmentCallbacks{

    private static final String TAG_HOME = "HOME";
    private static final String TAG_SETTINGS = "SETTINGS";
    private static final String TAG_ABOUT = "ABOUT";

    public static String CURRENT_TAG = TAG_HOME;

    private SharedPreferences sharedPreferences;
    private Handler mHandler;
    private BottomNavigationView navigation;


    private void loadFragment(){
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG)!=null){
            return;
        }
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                Fragment fragment = getFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                //fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.content,fragment,CURRENT_TAG);
                fragmentTransaction.commitNowAllowingStateLoss();
            }
        };

        if (mPendingRunnable!=null){
            mHandler.post(mPendingRunnable);
        }
    }

    private Fragment getFragment() {
        switch (CURRENT_TAG){
            case TAG_HOME:
                return new RecentFragment();
            case TAG_SETTINGS:
                return new SettingFragment();
            case TAG_ABOUT:
                return new FavoriteFragment();
            default:
                return new RecentFragment();
        }
    }


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    CURRENT_TAG=TAG_HOME;
                    break;
                case R.id.navigation_setting:
                    CURRENT_TAG=TAG_SETTINGS;
                    break;
                case R.id.navigation_favorites:
                    CURRENT_TAG=TAG_ABOUT;
                    break;
            }
            loadFragment();
            return true;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayShowTitleEnabled(false);

        CURRENT_TAG=TAG_HOME;
        mHandler = new Handler();

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        loadFragment();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.options,menu);
        if (isMyServiceRunning(FloatingWindowService.class)){
            menu.findItem(R.id.action_settings).setTitle(R.string.service_disable);
        }
        else {
            menu.findItem(R.id.action_settings).setTitle(R.string.service_enable);
        };
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if (id == R.id.action_settings){
            if (isMyServiceRunning(FloatingWindowService.class)){
                stopService(new Intent(this, FloatingWindowService.class));
                item.setTitle(R.string.service_enable);
            }
            else {
                startService(new Intent(this, FloatingWindowService.class));
                item.setTitle(R.string.service_disable);
            }
        }
        ReloadFragment();
        return true;
    }


    @Override
    protected void onActivityResult(int requestcode, int resultcode, Intent data) {
        super.onActivityResult(requestcode,resultcode,data);
    }

    @Override
    public void ReloadFragment() {
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                Fragment fragment = getFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.content,fragment,CURRENT_TAG);
                fragmentTransaction.commitNowAllowingStateLoss();
            }
        };

        if (mPendingRunnable!=null){
            mHandler.post(mPendingRunnable);
        }
    }

    @Override
    public void ChangeFragment(int id) {
        View child = navigation.findViewById(id);
        child.performClick();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
