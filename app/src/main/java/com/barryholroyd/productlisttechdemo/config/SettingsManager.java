package com.barryholroyd.productlisttechdemo.config;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.barryholroyd.productlisttechdemo.cache_disk.CacheDiskImage;
import com.barryholroyd.productlisttechdemo.cache_memory.CacheMemoryImage;
import com.barryholroyd.productlisttechdemo.product_info.ImageLoader;

import static com.barryholroyd.productlisttechdemo.config.Settings.*;

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
    /** Standard share preferences object. */
    private static SharedPreferences sp = null;

    /**
     * OnSharedPreferenceChangeListener instance.
     * It must be stored as a static field (as opposed to a local variable) because
     * registerOnSharedPreferenceChangeListener() does not retain a strong reference to it.
     */
    static private SettingsManager.OnSharedPreferenceChangeListenerWm
            onSharedPreferenceChangeListenerWm;

    /** Initialization. */
    public static void init(Activity a) {
        // use app-level shared preferences
        sp = PreferenceManager.getDefaultSharedPreferences(a);

        // Initialize the callback that handles changes to persistent data.
        onSharedPreferenceChangeListenerWm = new OnSharedPreferenceChangeListenerWm();
        sp.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListenerWm);
    }

    /**
     * Share preferences callback.
     *
     * Called when the user makes a change to the app's settings. This updates variables stored
     * statically in Settings as well as takes any action appropriate to the changed setting
     * (e.g., resetting the memory or disk cache if the cache max size changed).
     */
    private static class OnSharedPreferenceChangeListenerWm
            implements SharedPreferences.OnSharedPreferenceChangeListener
    {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            switch (Settings.Keys.valueOf(key)) {
                case APP_USE_THREADS:
                    Settings.appUseThreads = calcAppUseThreads(sp);
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
                    Settings.memoryCacheByBytes = calcMemoryCacheByBytes(sp);
                    ImageLoader.cacheMemory = Settings.isMemoryCacheByBytes()
                            ? CacheMemoryImage.createWithBytes(Settings.getMemoryCacheSizeBytes())
                            : CacheMemoryImage.createWithPercent(Settings.getMemoryCacheSizePercent());
                    break;
                case MEMORY_CACHE_SIZE_PERCENT:
                    Settings.memoryCacheSizePercent = Settings.calcMemoryCacheSizePercent(sp);
                    if (!Settings.isMemoryCacheByBytes()) {
                        ImageLoader.cacheMemory = CacheMemoryImage.createWithPercent(
                                memoryCacheSizePercent);
                    }
                    break;
                case MEMORY_CACHE_SIZE:
                    Settings.memoryCacheSizeBytes = Settings.calcMemoryCacheSizeBytes(sp);
                    if (Settings.isMemoryCacheByBytes()) {
                        ImageLoader.cacheMemory =
                                CacheMemoryImage.createWithBytes(Settings.memoryCacheSizeBytes);
                    }
                    break;
                case DISK_CACHE_ON:
                    Settings.diskCacheOn = sp.getBoolean(key, true);
                    break;
                case DISK_CACHE_TRACE:
                    Settings.diskCacheTrace = sp.getBoolean(key, false);
                    break;
                case DISK_CACHE_SIZE:
                    Settings.diskCacheSizeBytes = Settings.calcDiskCacheSizeBytes(sp);
                    CacheDiskImage.setMaxCacheSize(Settings.diskCacheSizeBytes);
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
