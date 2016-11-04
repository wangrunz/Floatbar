package runzhong.floatbar;

import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button button;
    public final static int REQUEST_CODE= 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button=(Button)findViewById(R.id.button);


        button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(!Settings.canDrawOverlays(MainActivity.this)){
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:"+getPackageName()));
                    startActivityForResult(intent,REQUEST_CODE);
                }
                else {
                    startService(new Intent(MainActivity.this,FloatingWindow.class));
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
}
