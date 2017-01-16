package com.barryholroyd.productlisttechdemo.preferences;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.barryholroyd.productlisttechdemo.R;

/**
 * Fragment to handle preferences.
 */
public class SettingsFragment extends PreferenceFragment {
    /**
     * Standard onCreate() method.
     *
     * Adds the preferences from XML.
     *
     * @param savedInstanceState    standard Bundle.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource.
        addPreferencesFromResource(R.xml.preferences);
    }
}