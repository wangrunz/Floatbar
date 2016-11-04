package runzhong.floatbar;

import android.app.ActivityManager;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

    private Switch switcher;
    public final static int REQUEST_CODE= 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switcher = (Switch)findViewById(R.id.switch1);
        if(isMyServiceRunning(FloatingWindow.class)){
            switcher.toggle();
        }

        switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    if(!Settings.canDrawOverlays(MainActivity.this)){
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()));
                        startActivityForResult(intent,REQUEST_CODE);
                    }
                    else {
                        startService(new Intent(MainActivity.this,FloatingWindow.class));
                    }
                }else{
                   stopService(new Intent(MainActivity.this,FloatingWindow.class));
                }
            }
        });
    }

    @Override
    protected void  onActivityResult(int requestcode, int resultcode, Intent data){
        if (requestcode==REQUEST_CODE)
        {
            if(Settings.canDrawOverlays(this)){
                startService(new Intent(MainActivity.this,FloatingWindow.class));
            }
        }
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
