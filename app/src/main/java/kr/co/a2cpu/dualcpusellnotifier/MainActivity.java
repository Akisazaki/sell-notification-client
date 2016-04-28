package kr.co.a2cpu.dualcpusellnotifier;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity {

    private static final String SERVICE_INTENT = "kr.co.2cpu.sell.notify.service";

    private Switch serviceSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serviceSwitch = (Switch) findViewById(R.id.serviceSwitch);
        serviceSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startService(new Intent(SERVICE_INTENT));
                } else {
                    stopService(new Intent(SERVICE_INTENT));
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        serviceSwitch.setChecked(isServiceRunning());
    }

    private boolean isServiceRunning() {
        final String serviceName = NotifyService.class.getName();
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo info : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(info.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
