package edu.neu.ccs.wellness.storytelling;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import edu.neu.ccs.wellness.storytelling.settings.SettingsActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.button_setting).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSettingActivity();
            }
        });

        /*
        Button buttonLogout = findViewById(R.id.button_logout);
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutAndStartLoginActivity();
            }
        });
        buttonLogout.setText(String.format(
                getString(R.string.logout_user_button), getUserId()));
        */

        TextView userInfoTextView= findViewById(R.id.user_login_info_text);
        userInfoTextView.setText(String.format(getString(R.string.appinfo_user_info), getUserId()));

        TextView versionTextView = findViewById(R.id.textVersionInfo);
        versionTextView.setText(getVersionText());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.about_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_logout_user:
                showLogoutDialog(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    private void showLogoutDialog(Context context) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(
                context, R.style.AppTheme_Dialog);
        alertBuilder.setTitle(R.string.appinfo_dialog_logout_title);
        alertBuilder.setMessage(R.string.appinfo_dialog_logout_msg);
        alertBuilder.setPositiveButton(R.string.appinfo_dialog_logout_positive_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        logoutAndStartLoginActivity();
                        dialog.dismiss();
                    }
        });
        alertBuilder.setNegativeButton(R.string.appinfo_dialog_logout_negative_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
        });
        alertBuilder.show();
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
