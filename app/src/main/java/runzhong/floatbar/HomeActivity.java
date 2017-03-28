package runzhong.floatbar;

import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity implements SettingFragment.ReloadCallbacks,HomeFragment.ChangeFragmentCallbacks{

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
                return new HomeFragment();
            case TAG_SETTINGS:
                return new SettingFragment();
            case TAG_ABOUT:
                return new AboutFragment();
            default:
                return new HomeFragment();
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
                case R.id.navigation_about:
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

        CURRENT_TAG=TAG_HOME;
        mHandler = new Handler();

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        loadFragment();

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
}
