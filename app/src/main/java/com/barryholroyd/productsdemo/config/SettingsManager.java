package com.barryholroyd.productsdemo.config;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static com.barryholroyd.productsdemo.config.Settings.*;

/**
 * SettingsManager catches changes the user makes to the preferences in the App Bar's
 * menu's Settings menu item and updates the corresponding runtime variables in Settings.
 * All changes to those preferences are automatically stored in SharedPreferences.
 *
 * The documentation says that the PreferenceManager does not store a strong reference to
 * the OnSharedPreferenceChangeListener instance that we pass it, so we need to -- otherwise,
 * it might get garbage collected at any time.
 */

public class SettingsManager {
    static private SharedPreferences sp = null;
    static private SettingsManager.OnSharedPreferenceChangeListenerWm
            onSharedPreferenceChangeListenerEm;

    static public void init(Activity a) {
        // use app-level shared preferences
        sp = PreferenceManager.getDefaultSharedPreferences(a);

        // Initialize the callback that handles changes to persistent data.
        onSharedPreferenceChangeListenerEm = new SettingsManager.OnSharedPreferenceChangeListenerWm();
        sp.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListenerEm);
    }

    static private class OnSharedPreferenceChangeListenerWm
            implements SharedPreferences.OnSharedPreferenceChangeListener
    {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (Settings.Keys.valueOf(key)) {
                case APP_USE_THREADS:
                    Settings.appUseThreads = getAppUseThreads(sp);
                    break;
                case APP_TRACE:
                    Settings.appTrace = sp.getBoolean(key, true);
                    break;
                case APP_TRACE_ALC:
                    Settings.appTraceAlc = sp.getBoolean(key, false);
                    break;
                case APP_TRACE_DETAILS:
                    Settings.appTraceDetails = sp.getBoolean(key, false);
                    break;
                case APP_DISPLAY_URL:
                    Settings.appDisplayUrl = sp.getBoolean(key, false);
                    break;
                case MEMORY_CACHE_ON:
                    Settings.memoryCacheOn = sp.getBoolean(key, true);
                    break;
                case MEMORY_CACHE_TRACE:
                    Settings.memoryCacheTrace = sp.getBoolean(key, false);
                    break;
                case MEMORY_CACHE_SIZE_APPROACH:
                    Settings.memoryCacheBytes = getMemoryCacheBytes(sp);
                    break;
                case MEMORY_CACHE_SIZE_PERCENT:
                    Settings.getMemoryCacheSizePercent(sp);
                    break;
                case MEMORY_CACHE_SIZE_MEGABYTES:
                    Settings.getMemoryCacheSizeBytes(sp);
                    break;
                case DISK_CACHE_ON:
                    Settings.diskCacheOn = sp.getBoolean(key, true);
                    break;
                case DISK_CACHE_TRACE:
                    Settings.diskCacheTrace = sp.getBoolean(key, false);
                    break;
                case DISK_CACHE_DC_SIZE_KILOBYTES:
                    Settings.getDiskCacheSizeBytes(sp);
                    break;
                case DISK_CACHE_CLEAR:
                    Settings.diskCacheClear = sp.getBoolean(key, true);
                    break;
                case IMAGE_LOADER_TRACE:
                    Settings.imageLoaderTrace = sp.getBoolean(key, false);
                    break;
                case NETWORK_TRACE:
                    Settings.networkTrace = sp.getBoolean(key, false);
                    break;
            }
        }
    }
}
