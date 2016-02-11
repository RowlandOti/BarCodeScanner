package com.rowland.scanner.ui.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.rowland.scanner.R;

/**
 * Created by saj on 27/01/15.
 */
public class SettingsActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);


    }
}
