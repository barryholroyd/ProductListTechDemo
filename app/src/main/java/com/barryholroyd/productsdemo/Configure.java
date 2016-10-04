package com.barryholroyd.productsdemo;

/** Class with static constants to configure the app. */
final class Configure {
    static class App {
        /** If true, use Thread for image loading, otherwise use AsyncTask. */
        static final boolean USE_THREADS = false;

        /** Tracing flag for app-level logging. */
        static final boolean TRACE = true;

        /** Tracing flag for activity lifecycle events. */
        static final boolean TRACE_ALC = true;

        /** Tracing flag for detailed cache logging. */
        static final boolean TRACE_DETAILS = false;

        /** Toggle display of image url in name field (for debugging). */
        static final boolean DISPLAY_URL = true;
    }

    /** ImageLoader configuration. */
    static class ImageLoader {
        /** Tracing flag for ImageLoader logging. */
        static final boolean TRACE = false;
    }

    /** Memory cache configuration. */
    static class MemoryCache {
        /** Memory cache toggle */
        static final boolean ON = false;

        /** Tracing flag for the image memory cache. */
        static final boolean TRACE = false;

        /** Specify memory cache in percent of total memory or in bytes. */
        static final boolean PERCENT = false;

        /**
         * Percent of total memory to use for the memory cache.
	 * Relevant iff PERCENT == true.
         */
        static final int SIZE_PERCENT = 10;

        /**
         * Memory (in bytes) to allocate for the memory cache.
	 * Relevant iff PERCENT == false.
         */
        static final long MC_SIZE_BYTES = 2*1024*1024;
    }

    /** Disk cache configuration. */
    static class DiskCache {
        /** Disk cache toggle. */
        static final boolean ON = false;

        /** Tracing flag for the image disk cache. */
        static final boolean TRACE = false;

        /** Disk space (in bytes) to allocate for the disk cache. */
        static final long DC_SIZE_BYTES = 500000;

        /** Clear the disk cache when the app starts up. */
        static final boolean CLEAR = false;

        /** Names for the cache directories. */
        static final String CACHE_DIR = "images";
    }

    /** Memory cache configuration. */
    static class Network {
        /** Tracing flag for the networking module. */
        static final boolean TRACE = true;
    }
}
