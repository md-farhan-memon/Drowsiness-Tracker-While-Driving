package nodomain.freeyourgadget.gadgetbridge.com.sjain;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.activities.ControlCenterv2;
import nodomain.freeyourgadget.gadgetbridge.com.sjain.MyAcclerationSpeed.MainActivity;

public class LandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_landing);
        findViewById(R.id.btn_pulse_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GBApplication.getERPrefs().isLoggedIn()) {
                    Intent i = new Intent(LandingActivity.this, ControlCenterv2.class);
                    startActivity(i);
                } else {
                    Intent i = new Intent(LandingActivity.this, LoginActivity.class);
                    startActivity(i);
                }
            }
        });
        findViewById(R.id.btn_blackspot_app).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (GBApplication.getERPrefs().isLoggedIn()) {
                    Intent i = new Intent(LandingActivity.this, MainActivity.class);
                    startActivity(i);
                } else {
                    Intent i = new Intent(LandingActivity.this, LoginActivity.class);
                    startActivity(i);
                }
            }
        });
    }
}
