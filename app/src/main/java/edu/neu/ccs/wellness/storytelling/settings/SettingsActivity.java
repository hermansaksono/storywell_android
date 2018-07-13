package edu.neu.ccs.wellness.storytelling.settings;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by hermansaksono on 7/13/18.
 */

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new UserSettingFragment())
                .commit();
    }
}
