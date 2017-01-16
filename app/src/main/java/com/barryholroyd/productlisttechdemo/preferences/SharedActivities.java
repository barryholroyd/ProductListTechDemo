package com.barryholroyd.productlisttechdemo.preferences;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.barryholroyd.productlisttechdemo.R;

/**
 * Activity that provides common appbar menu preferences.
 * <p>
 * These can be used by any Activity that wants to display and use the AppBar menu.
 */
public abstract class SharedActivities extends AppCompatActivity {
    /**
     * Create the standard options menu for the app bar.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_appbar, menu);
        return true;
    }

    /** Standard menu item selection callback. */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
