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
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity{

    public final static int REQUEST_CODE = 666;
    private static final String TAG_HOME = "HOME";
    private static final String TAG_SETTINGS = "SETTINGS";
    private static final String TAG_ABOUT = "ABOUT";

    public static String CURRENT_TAG = TAG_HOME;

    private SharedPreferences sharedPreferences;
    private Handler mHandler;


    private void loadFragment(){
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG)!=null){
            return;
        }
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

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key),MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (isMyServiceRunning(FloatingWindow.class)){
            editor.putBoolean(getString(R.string.preference_fab_switch_key),true);
        }
        else {
            editor.putBoolean(getString(R.string.preference_fab_switch_key),false);
        }
        editor.commit();

        loadFragment();

    }

    @Override
    protected void onActivityResult(int requestcode, int resultcode, Intent data) {
        super.onActivityResult(requestcode,resultcode,data);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(this.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
