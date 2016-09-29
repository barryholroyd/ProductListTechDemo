package com.barryholroyd.walmartproducts;

/**
 * Class with static constants to configure the app. These are mostly used for
 */

final class Configure {
    /*
     * Main Flags
     */
    /** If true, use Thread for image loading, otherwise use AsyncTask. */
    static final boolean USE_THREADS = true;

    /** Memory cache toggle */
    static final boolean MC_ON = true;

    /** Disk cache toggle. */
    static final boolean DC_ON = false;

    /*
     * Memory Cache.
     */
    /** Specify memory cache in percent of total memory or in bytes. */
    static final boolean MC_PERCENT = true;

    /** Percent of total memory to use for the memory cache. Relevant iff MC_PERCENT == true. */
    static final int  MC_CACHESIZE_PERCENT = 10;

    /** Memory (in bytes) to allocate for the memory cache. Relevant iff MC_PERCENT == false. */
    static final long MC_CACHESIZE_BYTES   = 10000;

    /*
     * Disk Cache.
     */
    /** Disk space (in bytes) to allocate for the disk cache. */
    static final long DISK_CACHESIZE_BYTES = 1000000;
    // static final long DISK_CACHESIZE_BYTES = 10000; // DEL:

    /*
     * Tracing.
     */

    /** Tracing flag for the image memory cache. */
    static final boolean blobCacheMemoryTrace = true;

    /** Tracing flag for the image disk cache. */
    static final boolean imageCacheDiskTrace = true;

}
