package com.barryholroyd.productlisttechdemo.config;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import static com.barryholroyd.productlisttechdemo.config.Settings.Keys.*;

/** Class with static constants to configure the app. */
public final class Settings {
    /** Names for the cache directories. */
    public static final String CACHE_DIR = "images";

    /** Keys will be used as Strings for SharePreferences. */
    enum Keys {
        APP_USE_THREADS, APP_TRACE, APP_TRACE_ALC, APP_TRACE_DETAILS, APP_DISPLAY_URL,
        MEMORY_CACHE_ON, MEMORY_CACHE_TRACE, MEMORY_CACHE_SIZE_APPROACH, MEMORY_CACHE_SIZE_PERCENT,
        MEMORY_CACHE_SIZE,
        DISK_CACHE_ON, DISK_CACHE_TRACE, DISK_CACHE_SIZE, DISK_CACHE_CLEAR,
        IMAGE_LOADER_TRACE, NETWORK_TRACE
    }

    /**
     * Initialize all the local variables that are stored as share preferences.
     *
     * @param a the current Activity.
     */
    public static void init(Activity a) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(a);

        /*
         * NOTE: initial default values are set in preferences.xml.
         */
        appUseThreads	        = calcAppUseThreads(sp);
        appTrace		        = sp.getBoolean(APP_TRACE.name(), true);
        appTraceAlc		        = sp.getBoolean(APP_TRACE_ALC.name(), false);
        appTraceDetails		    = sp.getBoolean(APP_TRACE_DETAILS.name(), false);
        appDisplayUrl		    = sp.getBoolean(APP_DISPLAY_URL.name(), false);
        memoryCacheOn		    = sp.getBoolean(MEMORY_CACHE_ON.name(), true);
        memoryCacheTrace	    = sp.getBoolean(MEMORY_CACHE_TRACE.name(), false);
        memoryCacheByBytes      = calcMemoryCacheByBytes(sp);
        memoryCacheSizePercent	= calcMemoryCacheSizePercent(sp);
        memoryCacheSizeBytes    = calcMemoryCacheSizeBytes(sp);
        diskCacheOn		        = sp.getBoolean(DISK_CACHE_ON.name(), true);
        diskCacheTrace		    = sp.getBoolean(DISK_CACHE_TRACE.name(), false);
        diskCacheSizeBytes	    = calcDiskCacheSizeBytes(sp);
        diskCacheClear		    = sp.getBoolean(DISK_CACHE_CLEAR.name(), true);
        imageLoaderTrace	    = sp.getBoolean(IMAGE_LOADER_TRACE.name(), false);
        networkTrace		    = sp.getBoolean(NETWORK_TRACE.name(), false);
    }

    /*
     * Conversion routines that take a String key and return a value of the
     * desired type.
     */
    static boolean calcAppUseThreads(SharedPreferences sp) {
        return sp.getString(APP_USE_THREADS.name(), "Threads").equals("Threads");
    }

    static boolean calcMemoryCacheByBytes(SharedPreferences sp) {
        return sp.getString(MEMORY_CACHE_SIZE_APPROACH.name(), "Bytes").equals("Bytes");
    }

    static int calcMemoryCacheSizePercent(SharedPreferences sp) {
        return readPrefInt(sp, MEMORY_CACHE_SIZE_PERCENT.name(), 10);
    }

    static long calcMemoryCacheSizeBytes(SharedPreferences sp) {
        return readPrefInt(sp, MEMORY_CACHE_SIZE.name(), 2) * 1024 * 1024;
    }

    static int calcDiskCacheSizeBytes(SharedPreferences sp) {
        return readPrefInt(sp, DISK_CACHE_SIZE.name(), 50) * 1024 * 1024;
    }

    /*
     * The following method is necessary because the Preferences
     * menu system stores ints and floats as Strings (even though the
     * SharedPreferences system can store integers and provides a
     * getInt() method).
     */
    private static int readPrefInt(SharedPreferences settings, String key, int default_val) {
    	String val_str = settings.getString(key, Integer.toString(default_val));
    	return Integer.parseInt(val_str);
    }

    /******************
     * App
     ******************/
    /** If true, use Thread for image loading, otherwise use AsyncTask. */
    static boolean appUseThreads = true;

    /** Tracing flag for app-level logging. */
    static boolean appTrace = true;

    /** Tracing flag for activity lifecycle events. */
    static boolean appTraceAlc = false;

    /** Tracing flag for detailed disk and network cache logging (flagged with "DETAILS"). */
    static boolean appTraceDetails = false;

    /** Toggle display of image url in name field (for debugging). */
    static boolean appDisplayUrl = false;

    /******************
     * Memory Cache
     ******************/
    /** Memory cache toggle */
    static boolean memoryCacheOn = true;

    /** Tracing flag for the image memory cache. */
    static boolean memoryCacheTrace = false;

    /** Specify memory cache in percent of total memory or in bytes. */
    static boolean memoryCacheByBytes = true;

    /**
     * Percent of total memory to use for the memory cache.
     * Relevant iff memoryCacheByBytes == true.
     */
    static int memoryCacheSizePercent = 10;

    /**
     * Memory (in bytes) to allocate for the memory cache.
     * Relevant iff memoryCacheByBytes == false.
     */
    static long memoryCacheSizeBytes = 2*1024*1024;

    /******************
     * Disk Cache
     ******************/
    /** Disk cache toggle. */
    static boolean diskCacheOn = true;

    /** Tracing flag for the image disk cache. */
    static boolean diskCacheTrace = false;

    /** Disk space (in bytes) to allocate for the disk cache. */
    static long diskCacheSizeBytes = 500000;

    /** Clear the disk cache when the app starts up. */
    static boolean diskCacheClear = true;

    /******************
     * Miscellaneous
     ******************/
     /** Tracing flag for ImageLoader logging. */
    static boolean imageLoaderTrace = false;

     /** Tracing flag for the networking module. */
    static boolean networkTrace = false;

    /******************
     * Getters
     ******************/
    public static boolean isAppDisplayUrl() {
        return appDisplayUrl;
    }

    public static boolean isAppTrace() {
        return appTrace;
    }

    public static boolean isAppTraceAlc() {
        return appTraceAlc;
    }

    public static boolean isAppTraceDetails() {
        return appTraceDetails;
    }

    public static boolean isAppUseThreads() {
        return appUseThreads;
    }

    public static boolean isDiskCacheClear() {
        return diskCacheClear;
    }

    public static boolean isDiskCacheOn() {
        return diskCacheOn;
    }

    public static long getDiskCacheSizeBytes() {
        return diskCacheSizeBytes;
    }

    public static boolean isDiskCacheTrace() {
        return diskCacheTrace;
    }

    public static boolean isImageLoaderTrace() {
        return imageLoaderTrace;
    }

    public static boolean isMemoryCacheOn() {
        return memoryCacheOn;
    }

    public static boolean isMemoryCacheByBytes() {
        return memoryCacheByBytes;
    }

    public static long getMemoryCacheSizeBytes() {
        return memoryCacheSizeBytes;
    }

    public static int getMemoryCacheSizePercent() {
        return memoryCacheSizePercent;
    }

    public static boolean isMemoryCacheTrace() {
        return memoryCacheTrace;
    }

    public static boolean isNetworkTrace() {
        return networkTrace;
    }
}
