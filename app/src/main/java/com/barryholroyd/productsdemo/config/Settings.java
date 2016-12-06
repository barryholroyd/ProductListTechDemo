package com.barryholroyd.productsdemo.config;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/** Class with static constants to configure the app. */
public final class Settings {
    /** Names for the cache directories. */
    static public final String CACHE_DIR = "images";

    static public void init(Activity a) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(a);

        appUseThreads	        = sp.getString("APP_USE_THREADS", "Threads").equals("Threads");
        appTrace		        = sp.getBoolean("APP_TRACE", true);
        appTraceAlc		        = sp.getBoolean("APP_TRACE_ALC", false);
        appTraceDetails		    = sp.getBoolean("APP_TRACE_DETAILS", false);
        appDisplayUrl		    = sp.getBoolean("APP_DISPLAY_URL", false);
        memoryCacheOn		    = sp.getBoolean("MEMORY_CACHE_ON", true);
        memoryCacheTrace	    = sp.getBoolean("MEMORY_CACHE_TRACE", false);
        memoryCacheBytes        = sp.getString("MEMORY_CACHE_SIZE_APPROACH", "Bytes").equals("Bytes");
        memoryCacheSizePercent	= readPrefInt(sp, "MEMORY_CACHE_SIZE_PERCENT", 10);
        memoryCacheSizeBytes    = readPrefInt(sp, "MEMORY_CACHE_SIZE_MEGABYTES", 2) * 1024 * 1024 ;
        diskCacheOn		        = sp.getBoolean("DISK_CACHE_ON", true);
        diskCacheTrace		    = sp.getBoolean("DISK_CACHE_TRACE", false);
        diskCacheSizeBytes	    = readPrefInt(sp, "DISK_CACHE_DC_SIZE_KILOBYTES", 500) * 1024;
        diskCacheClear		    = sp.getBoolean("DISK_CACHE_CLEAR", true);
        imageLoaderTrace	    = sp.getBoolean("IMAGE_LOADER_TRACE", false);
        networkTrace		    = sp.getBoolean("NETWORK_TRACE", false);
    }

    /*
     * The following methods are necessary because the Preferences
     * menu system stores ints and floats as Strings (even though the
     * SharedPreferences system can store integers and provides a
     * getInt() method).
     */
    static private int readPrefInt(SharedPreferences settings, String key, int default_val) {
    	String val_str = settings.getString(key, Integer.toString(default_val));
    	return Integer.parseInt(val_str);
    }

    /******************
     * App
     ******************/
    /** If true, use Thread for image loading, otherwise use AsyncTask. */
    static private boolean appUseThreads = true;

    /** Tracing flag for app-level logging. */
    static private boolean appTrace = true;

    /** Tracing flag for activity lifecycle events. */
    static private boolean appTraceAlc = false;

    /** Tracing flag for detailed cache logging. */
    static private boolean appTraceDetails = false;

    /** Toggle display of image url in name field (for debugging). */
    static private boolean appDisplayUrl = false;

    /******************
     * Memory Cache
     ******************/
    /** Memory cache toggle */
    static private boolean memoryCacheOn = true;

    /** Tracing flag for the image memory cache. */
    static private boolean memoryCacheTrace = false;

    /** Specify memory cache in percent of total memory or in bytes. */
    static private boolean memoryCacheBytes = true;

    /**
     * Percent of total memory to use for the memory cache.
     * Relevant iff memoryCacheBytes == true.
     */
    static private int memoryCacheSizePercent = 10;

    /**
     * Memory (in bytes) to allocate for the memory cache.
     * Relevant iff memoryCacheBytes == false.
     */
    static private long memoryCacheSizeBytes = 2*1024*1024;

    /******************
     * Disk Cache
     ******************/
    /** Disk cache toggle. */
    static private boolean diskCacheOn = true;

    /** Tracing flag for the image disk cache. */
    static private boolean diskCacheTrace = false;

    /** Disk space (in bytes) to allocate for the disk cache. */
    static private long diskCacheSizeBytes = 500000;

    /** Clear the disk cache when the app starts up. */
    static private boolean diskCacheClear = true;

    /******************
     * Miscellaneous
     ******************/
     /** Tracing flag for ImageLoader logging. */
    static private boolean imageLoaderTrace = false;

     /** Tracing flag for the networking module. */
    static private boolean networkTrace = false;

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

    public static boolean isMemoryCacheBytes() {
        return memoryCacheBytes;
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
