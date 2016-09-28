package com.barryholroyd.walmartproducts;

/**
 * Class with static constants to configure the app. These are mostly used for
 */

final class Configure {
    /** If true, use Thread for image loading, otherwise use AsyncTask. */
    static final boolean USE_THREADS = true;

    /** Specify memory cache in percent of total memory or in bytes. */
    static final boolean MC_PERCENT = true;

    /** Percent of total memory to use for the memory cache. Relevant iff MC_PERCENT == true. */
    static final int  MC_CACHESIZE_PERCENT = 10;

    /** Memory (in bytes) to allocate for the memory cache. Relevant iff MC_PERCENT == false. */
    static final long MC_CACHESIZE_BYTES   = 10000;

    /** Tracing flag for the image memory cache. */
    static final boolean blobCacheMemoryTrace = false;

    /** Tracing flag for the image disk cache. */
    static final boolean imageCacheDiskTrace = false;

    /** Disk space (in bytes) to allocate for the disk cache. */
    static final long DISK_CACHESIZE_BYTES = 1000000;
    // static final long DISK_CACHESIZE_BYTES = 10000; // DEL:
}
