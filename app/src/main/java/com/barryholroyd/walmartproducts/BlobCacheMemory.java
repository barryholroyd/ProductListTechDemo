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
 * By default, size is specified as the number of entries. However, if sizeOf()
 * is overridden it can be specified as the maxmimum number of bytes.
 * <p>
 * The class is instantiable so that:
 * <ol>
 *     <li>it can be extended and sizeOf() overridden.
 *     <li>multiple caches can be used in the same app.
 * </ol>
 */
public class BlobCacheMemory<K,V>
{
    /** Internal key/value mapping for cache storage. */
    final HashMap<K,V> bcmHm  = new HashMap<>();

    /** First in / first out tracking for deleting cache entries. */
    final LinkedList<K> bcmLl = new LinkedList<>();

    /** Maximum size of the cache. */
    int maxCacheSize = 0;

    /** Current cache size. */
    int currentCacheSize;

    /**
     * Set the maximum cache size.
     *
     * @param _maxCacheSize
     */
    void setCacheSize(int _maxCacheSize) {
        maxCacheSize = _maxCacheSize;
    }

    /**
     * Set the cache size as a percentage of overall maximum memory.
     * <p>
     * If you do this, you'll need to override sizeOf() also.
     *
     * @param percentage
     */
    void setCacheSizePercentMaxMemory(int percentage) {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        maxCacheSize = (percentage / 100) * maxMemory;
        // DEL: delete when done.
        Support.logd(String.format("Memory Cache Size: %d%% of %d = %d",
                percentage, maxMemory = maxCacheSize));
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
        System.out.format("Adding key: %s (cur=%d, max=%d)\n",
                key, currentCacheSize, maxCacheSize);

        // Disallow null keys.
        if (key == null) {
            throw new IllegalStateException("BlobCacheMemory: null key.");
        }

        if (maxCacheSize == 0) {
            throw new IllegalStateException("Cache size not initialized.");
        }

	    // Don't allow overwriting a key's value.
        if (get(key) != null) {
            System.out.format("Already added this key... returning\n");
            return;
	    }

        int valSize = sizeOf(key, val);
        System.out.format("Key value's size: %d (cur=%d,max=%d)\n",
            valSize, currentCacheSize, maxCacheSize);

        while ((currentCacheSize + valSize) > maxCacheSize) {
          K lastKey = bcmLl.removeLast();
          if (lastKey == null) {
              throw new IllegalStateException(
                  "BlobCacheMemory: null key when removing entries.");
          }
          V lastVal = bcmHm.remove(lastKey);
          int lastValSize = sizeOf(lastKey, lastVal);
          currentCacheSize -= lastValSize;
          System.out.format("Removing key (%s: %d). New cur=%d.",
                  lastKey, lastValSize, currentCacheSize);
        }
        currentCacheSize += valSize;

        bcmHm.put(key, val);

        System.out.format("Key added: %s (cur=%d, max=%d)\n",
          key, currentCacheSize, maxCacheSize);
        }
}
