package com.barryholroyd.productsdemo.preferences;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.barryholroyd.productsdemo.R;

/**
 * Created by Barry on 12/6/2016.
 */

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource.
        addPreferencesFromResource(R.xml.preferences);
    }
}