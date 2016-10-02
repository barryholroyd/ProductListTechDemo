package com.barryholroyd.productsdemo;

import android.graphics.Bitmap;

/**
 * Memory cache system extending CacheMemoryBlob to be specific to a String
 * (url) for the key and a Bitmap for the blob. sizeOf() is overridden so that
 * we can specify the maximum amount of storage allocated using bytes for the
 * unit size.
 * <p>
 * Static factory methods are used to create new instances so that the
 * maximum cache size can be specified in either bytes or percentage of
 * the total available memory (if implemented using constructors, the
 * constructors would have the same signature and hence be indistinguishable).
 */
class CacheMemoryImage extends CacheMemoryBlob<String, Bitmap> {
    /** Private constructor. */
    private CacheMemoryImage(long bytes) {
        super(bytes);
    }

    /**
     * Set the cache size as a percentage of overall maximum memory.
     * <p>
     * If you do this, you'll need to override sizeOf() also.
     *
     * @param percent percentage of the available memory to use for
     *                the memory cache.
     */
    static public CacheMemoryImage createWithPercent(int  percent) {
        return createWithBytes(convertPercentToBytes(percent));
    }

    /**
     * Set the maximum cache size in bytes.
     *
     * @param maxCacheSize maximum cache size in bytes
     */
    static public CacheMemoryImage createWithBytes(long maxCacheSize) {
        CacheMemoryImage icm = new CacheMemoryImage(maxCacheSize);
        return icm;
    }

    /** Calculate the number of bytes for the cache based on a percentage
     *  of the total available memory.
     *
     * @param percent percentage of the available memory to use for
     *                the memory cache.
     * @return bytes for the cache.
     */
    static private long convertPercentToBytes(int percent) {
        long maxMemory = Runtime.getRuntime().maxMemory();
        long maxCacheSize = (long) ((percent / 100f) * maxMemory);

        trace(String.format("Calculating maximum cache size: %d (%d%% of %d)",
                maxCacheSize, percent, maxMemory));

        return maxCacheSize;
    }

    /**
     * Return the size of a specific cached item instance.
     *
     * @param key the key for the bitmap being stored.
     * @param bm  the bitmap being stored.
     * @return    the size of the bitmap being stored.
     */
    @Override
    protected long sizeOf(String key, Bitmap bm) {
        return bm.getByteCount();
    }
}
