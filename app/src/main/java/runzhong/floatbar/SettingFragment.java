package runzhong.floatbar;


import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    public final static int REQUEST_CODE = 666;
    private ReloadCallbacks mCallbacks;

    public interface ReloadCallbacks{
        public void ReloadFragment();
    }
    private Context context;

    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        if (isMyServiceRunning(FloatingWindowService.class)){
            editor.putBoolean(getString(R.string.preference_enable_switch_key),true);
        }
        else {
            editor.putBoolean(getString(R.string.preference_enable_switch_key),false);
        }
        editor.commit();

        addPreferencesFromResource(R.xml.support_preferences);
        Preference resetPreference = findPreference(getString(R.string.preference_reset_key));
        resetPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                editor.putBoolean(getString(R.string.preference_enable_switch_key),false)
                                        .putString(getString(R.string.preference_fab_color_key),getString(R.string.preference_fab_color_default))
                                        .putString(getString(R.string.preference_fab_alpha_key),"100")
                                        .commit();
                                mCallbacks.ReloadFragment();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.preference_reset_msg)
                        .setCancelable(true)
                        .setNegativeButton(R.string.preference_reset_btn_no, listener)
                        .setPositiveButton(R.string.preference_reset_btn_yes, listener)
                        .show();
                return false;
            }
        });
        Preference clearPreference = findPreference(getString(R.string.preference_clear_key));
        clearPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                sendMsg(getString(R.string.action_db_clear));
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(R.string.preference_clear_msg)
                        .setCancelable(true)
                        .setNegativeButton(R.string.preference_clear_btn_yes, listener)
                        .setPositiveButton(R.string.preference_clear_btn_no, listener)
                        .show();
                return false;
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(getString(R.string.preference_enable_switch_key))){
            boolean status = sharedPreferences.getBoolean(s,false);
            if (status){
                if (!Settings.canDrawOverlays(context)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + context.getPackageName()));
                    startActivityForResult(intent, REQUEST_CODE);
                } else {
                    context.startService(new Intent(context, FloatingWindowService.class));
                }
            }
            else {
                context.stopService(new Intent(context, FloatingWindowService.class));
            }
        }
        else if (s.equals(getString(R.string.preference_fab_switch_key))){
            sendMsg(getString(R.string.action_fab_toggle));
        }
        else if (s.equals(getString(R.string.preference_fab_color_key))){
            String color = sharedPreferences.getString(s,"#000000");
            try{
                Color.parseColor(color);
            } catch (IllegalArgumentException e){
                Toast.makeText(context,R.string.preference_invalid_color_code,Toast.LENGTH_SHORT).show();
                sharedPreferences.edit()
                        .putString(getString(R.string.preference_fab_color_key),getString(R.string.preference_fab_color_default))
                        .commit();
                mCallbacks.ReloadFragment();
            }
            sendMsg(getString(R.string.action_fab_refresh));
        }
        else if (s.equals(getString(R.string.preference_fab_alpha_key))){
            String alpha = sharedPreferences.getString(s,"50");
            try{
                int i = Integer.parseInt(alpha);
                if (i>100 | i<0){
                    throw new NumberFormatException(alpha);
                }
            } catch (NumberFormatException e){
                Toast.makeText(context,R.string.preference_invalid_alpha_code,Toast.LENGTH_SHORT).show();
                sharedPreferences.edit()
                        .putString(getString(R.string.preference_fab_alpha_key),"50")
                        .commit();
                mCallbacks.ReloadFragment();
            }
            sendMsg(getString(R.string.action_fab_refresh));
        }
        else if (s.equals(getString(R.string.preference_fab_size_key))){
            sendMsg(getString(R.string.action_fab_refresh));
        }
    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        this.context=context;
        this.mCallbacks = (ReloadCallbacks) context;
    }

    @Override
    public void onResume(){
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onActivityResult(int requestcode, int resultcode, Intent data) {
        if (requestcode == REQUEST_CODE) {
            if (Settings.canDrawOverlays(context)) {
                context.startService(new Intent(context, FloatingWindowService.class));
            }
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void sendMsg(String action){
        Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
