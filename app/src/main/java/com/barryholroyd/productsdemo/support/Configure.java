package com.barryholroyd.productsdemo.support;

/** Class with static constants to configure the app. */
public final class Configure {
    static public class App {
        /** If true, use Thread for image loading, otherwise use AsyncTask. */
        static public final boolean USE_THREADS = true;

        /** Tracing flag for app-level logging. */
        static public final boolean TRACE = true;

        /** Tracing flag for activity lifecycle events. */
        static public final boolean TRACE_ALC = false;

        /** Tracing flag for detailed cache logging. */
        static public final boolean TRACE_DETAILS = false;

        /** Toggle display of image url in name field (for debugging). */
        static public final boolean DISPLAY_URL = false;
    }

    /** ImageLoader configuration. */
    static public class ImageLoader {
        /** Tracing flag for ImageLoader logging. */
        static public final boolean TRACE = false;
    }

    /** Memory cache configuration. */
    static public class MemoryCache {
        /** Memory cache toggle */
        static public final boolean ON = true;

        /** Tracing flag for the image memory cache. */
        static public final boolean TRACE = false;

        /** Specify memory cache in percent of total memory or in bytes. */
        static public final boolean PERCENT = false;

        /**
         * Percent of total memory to use for the memory cache.
	 * Relevant iff PERCENT == true.
         */
        static public final int SIZE_PERCENT = 10;

        /**
         * Memory (in bytes) to allocate for the memory cache.
	 * Relevant iff PERCENT == false.
         */
        static public final long MC_SIZE_BYTES = 2*1024*1024;
    }

    /** Disk cache configuration. */
    static public class DiskCache {
        /** Disk cache toggle. */
        static public final boolean ON = true;

        /** Tracing flag for the image disk cache. */
        static public final boolean TRACE = false;

        /** Disk space (in bytes) to allocate for the disk cache. */
        static public final long DC_SIZE_BYTES = 500000;

        /** Clear the disk cache when the app starts up. */
        public static final boolean CLEAR = true;

        /** Names for the cache directories. */
        static public final String CACHE_DIR = "images";
    }

    /** Memory cache configuration. */
    static public class Network {
        /** Tracing flag for the networking module. */
        static public final boolean TRACE = false;
    }
}
