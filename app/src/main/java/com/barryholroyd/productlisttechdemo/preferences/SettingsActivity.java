package com.barryholroyd.productlisttechdemo.preferences;

import android.app.Activity;
import android.os.Bundle;

/**
 * Activity to handle Android Preferences.
 */
public class SettingsActivity extends Activity {
    /**
     * Standard onCreate().
     *
     * Creates and registers a SettingsFragment instance with
     * the FragmentManager.
     *
     * @param savedInstanceState    standard Bundle.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
