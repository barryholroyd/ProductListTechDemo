package com.barryholroyd.walmartproducts;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Cache objects in memory.
 * <p>
 * I used ideas from LruCache for this implementation. I wrote my own cache
 * implementation just for the practice.
 * <p>
 * Either setCacheSize() or setCacheSizePercentMaxMemory() must be called to set
 * the maximum size of the cache.
 * <p>
 * LinkedHashMap could not be used for the internal hash map because it
 * doesn't allow multiple "oldest" entries to be identified and removed
 * for a single put request (i.e., for those situations where the item to
 * be inserted in the list is larger than the item to be removed from the
 * list and the list is currently at its absolute maximum, in terms of bytes.
 * <p>
 * By default, size is specified as the number of maximum number of bytes.
 * <p>
 * The class is instantiable (as opposed to being a singleton or having only
 * static methods) so that:
 * <ol>
 *     <li>it can be extended and sizeOf() overridden.
 *     <li>multiple caches can be used in the same app.
 * </ol>
 */
public class BlobCacheMemory<K,V>
{
    /** Debugging flag. */
    static boolean blobCacheMemoryTrace = false;

    /** Internal key/value mapping for cache storage. */
    final HashMap<K,V> bcmHm  = new HashMap<>();

    /** First in / first out tracking for deleting cache entries. */
    final LinkedList<K> bcmLl = new LinkedList<>();

    /** Maximum size of the cache in bytes. */
    long maxCacheSize = 0;

    /** Current cache size in bytes. */
    long currentCacheSize;

    /**
     * Set the maximum cache size in bytes.
     *
     * @param _maxCacheSize maximum cache size in bytes
     */
    void setCacheSize(long _maxCacheSize) {
        maxCacheSize = _maxCacheSize;
        trace(String.format("Maximum Cache Size: %d", maxCacheSize));
    }

    /**
     * Set the cache size as a percentage of overall maximum memory.
     * <p>
     * If you do this, you'll need to override sizeOf() also.
     *
     * @param percentage percentage of the available memory to use for
     *                   the memory cache.
     */
    void setCacheSizePercentMaxMemory(int percentage) {
        long maxMemory = Runtime.getRuntime().maxMemory();
        maxCacheSize = (long) (((float) percentage / 100) * maxMemory);
        trace(String.format("Maximum Cache Size: %d (%d%% of %d)",
                maxCacheSize, percentage, maxMemory));
    }

    /**
     * Size of each entry.
     * <p>
     * Defaults to 1, so that the cache size is specified as the
     * number of entries. However, overriding this allows  you to
     * specify the maximum amount of memory to be used in, in bytes,
     * if you override this method to return the size of each entry,
     * using the key and/or value provided here.
     */
    protected int sizeOf(K key, V val) {
        return 1;
    }

    /**
     * Get the item, if it exists.
     */
    public V get(K key) {
        return bcmHm.get(key);
    }

    /**
     * Add the specified key/value pair to the cache.
     * <p>
     * Ignore the request if the key is already present.
     */
    // TBD: ensure everything get synchroninzed correctly!
    public void add(K key, V val) {
        trace(String.format("Adding: %s", key));

        // Disallow null keys.
        if (key == null) {
            throw new BlobCacheMemoryException("null key.");
        }

        if (maxCacheSize == 0) {
            throw new BlobCacheMemoryException("cache size not initialized.");
        }

	    // Don't allow overwriting a key's value.
        if (get(key) != null) {
            Support.logd(String.format("Already added this key... returning\n"));
            return;
	    }

        int valSize = sizeOf(key, val);
	// DEL: when done
        Support.logd(String.format("  Sizes before: val=%d, cur=%d, max=%d\n",
                valSize, currentCacheSize, maxCacheSize));

	// Clear cache entries, if necessary.
        while ((currentCacheSize + valSize) > maxCacheSize) {
            if (bcmLl.isEmpty()) {
                /*
		 * This should only happen if the first object is larger
		 * than the entire cache.
		 */
                throw new BlobCacheMemoryException("cache is empty.");
            }

            K lastKey = bcmLl.removeLast();
            if (lastKey == null) {
              throw new BlobCacheMemoryException("null key when removing entries.");
            }
            V lastVal = bcmHm.remove(lastKey);
            int lastValSize = sizeOf(lastKey, lastVal);
            trace(String.format("Removing: %s (cachsize: %d - %d = %d).",
                    lastKey, currentCacheSize, lastValSize,
                    currentCacheSize - lastValSize));
            currentCacheSize -= lastValSize;
        }
        currentCacheSize += valSize;

	// Add to the cache.
        bcmHm.put(key, val);
        bcmLl.add(key);

	// DEL: when done
        Support.logd(String.format("  Sizes after: val=%d, cur=%d, max=%d\n",
                valSize, currentCacheSize, maxCacheSize));
    }

    private void trace(String msg) {
        Support.trace(blobCacheMemoryTrace, "Cache Memory", msg);
    }
}
