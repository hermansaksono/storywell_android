package edu.neu.ccs.wellness.storytelling;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import edu.neu.ccs.wellness.storytelling.settings.SettingsActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        findViewById(R.id.button_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSettingActivity();
            }
        });

        Button buttonLogout = findViewById(R.id.button_logout);
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutAndStartLoginActivity();
            }
        });
        buttonLogout.setText(String.format(
                getString(R.string.logout_user_button), getUserId()));

        TextView versionTextView = findViewById(R.id.textVersionInfo);
        versionTextView.setText(getVersionText());
    }

    private String getVersionText() {
        try {
            String versionName = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
            int versionCode = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionCode;
            return String.format(getString(R.string.appinfo_version), versionName, versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
    }

    private void startSettingActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void logoutAndStartLoginActivity() {
        new Storywell(this).logoutUser();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK );
        startActivity(intent);
        finish();
    }

    private String getUserId() {
        return FirebaseAuth.getInstance().getUid();
    }
}
