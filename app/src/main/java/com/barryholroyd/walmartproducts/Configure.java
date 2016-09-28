package com.barryholroyd.walmartproducts;

/**
 * TBD: docs
 */

final class Configure {
    /** If true, use Thread for image loading, otherwise use AsyncTask. */
    static final boolean USE_THREADS = true;

    /** Specify memory cache in percent of total memory or in bytes. */
    static final boolean MC_PERCENT = true;
    static final int  MC_CACHESIZE_PERCENT = 10;
    static final long MC_CACHESIZE_BYTES   = 10000;

    static final boolean blobCacheMemoryTrace = false;
}
