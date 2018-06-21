package nodomain.freeyourgadget.gadgetbridge.com.sjain;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;

public class LoginActivity extends AppCompatActivity {
    EditText etName, etPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etName = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);

        findViewById(R.id.btn_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!etName.getText().toString().isEmpty() && etName.getText().toString().compareTo(GBApplication.getERPrefs().getUserAdmin()) != 0) {
                    return;
                } else if (!etPassword.getText().toString().isEmpty() && etPassword.getText().toString().compareTo(GBApplication.getERPrefs().getPassword()) != 0) {
                    return;

                } else {
                    GBApplication.getERPrefs().setIsLoggedIn(true);
                }
            }
        });

    }
}
