package com.barryholroyd.walmartproducts;

/** Class with static constants to configure the app. */
final class Configure {
    /**
     * If true, use Thread for image loading, otherwise use AsyncTask.
     */
    static final boolean USE_THREADS = true;

    /**
     * Tracing flag for general logging.
     */
    static final boolean APP_TRACE = true;

    /**
     * Tracing flag for image disk cache details.
     */
    static final boolean TRACE_DETAILS = true;

    /**
     * Memory cache configuration.
     */
    static class MemoryCache {
        /**
         * Memory cache toggle
         */
        static final boolean MC_ON = false;

        /**
         * Tracing flag for the image memory cache.
         */
        static final boolean MC_TRACE = false;

        /**
         * Specify memory cache in percent of total memory or in bytes.
         */
        static final boolean MC_PERCENT = false;

        /**
         * Percent of total memory to use for the memory cache. Relevant iff MC_PERCENT == true.
         */
        static final int MC_SIZE_PERCENT = 10;

        /**
         * Memory (in bytes) to allocate for the memory cache. Relevant iff MC_PERCENT == false.
         */
        static final long MC_SIZE_BYTES = 2*1024*1024;
    }

    /**
     * Disk cache configuration.
     */
    static class DiskCache {
        /**
         * Disk cache toggle.
         */
        static final boolean DC_ON = true;

        /**
         * Tracing flag for the image disk cache.
         */
        static final boolean DC_TRACE = true;

        /**
         * Disk space (in bytes) to allocate for the disk cache.
         */
//      static final long DC_SIZE_BYTES = 500000;
        static final long DC_SIZE_BYTES = 100000; // DEL:

        /**
         * Clear the disk cache when the app starts up.
         */
        static final boolean DC_CLEAR = true;

        /**
         * Names for the cache directories.
         */
        static final String DC_CACHE_DIR = "images";
    }

    /**
     * Memory cache configuration.
     */
    static class Network {
        /**
         * Tracing flag for the networking module.
         */
        static final boolean NM_TRACE = false;
    }
}
