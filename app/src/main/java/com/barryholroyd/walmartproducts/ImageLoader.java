package com.barryholroyd.walmartproducts;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Load an image into an ImageView.
 * <p>
 * <ul>
 *     <li> Attempt to load from memory cache.
 *     <li> If that fails, load from background.
 *     <ul>
 *         <li>Attempt to load from disk cache.
 *         <li>If that fails, attempt to load from network.
 *         <li>When network load completes:
 *         <ul>
 *             <li>add to memory and disk caches
 *             <li>check to see if the URL loaded from is the same as the current
 *                 URL (it may have chanced if the containing ViewHolder got re-allocated).
 *             <li>If it is the same, then load it into the ImageView (in the main/UI thread).
 *             <li>If it is different, ignore and do nothing (the new URL will have already
 *                 been either loaded or queued to be loaded).
 *         </ul>
 *     </ul>
 * </ul>
 */
public class ImageLoader {
    /**
     * Singleton.
     * <p>
     * There are many ways of implementing singletons. This one is very simple and effective.
     * It initializes the singleton when the class is loaded. It avoids the double-checked
     * locking issue.
     *
     * @see <a href="http://www.javamex.com/tutorials/double_checked_locking.shtml">
     *      Double Checked Locking</a>
     */
    static final public ImageLoader instance = new ImageLoader();

    /**
     * If true, use threads to handle background loading; otherwise, use AsyncTask.
     * I've implemented both approaches for practice.
     */
    static private final boolean USE_THREADS = true;

    /** In-memory caching instance. */
    static private ImageCacheMemory cacheMemory;

    private ImageLoader() {
        cacheMemory = new ImageCacheMemory();
        cacheMemory.setCacheSizePercentMaxMemory(10);
    };

    void load(final ImageView iv, final String url) {
        Bitmap bitmap =  cacheMemory.get(url);
        if (bitmap != null) {
            iv.setImageBitmap(bitmap);
        }
        else {
            if (USE_THREADS) ImageLoaderThreads.load(iv, url, cacheMemory);
            // TBD: else imageLoaderAsyncTask.load(iv, url);
        }
    }

    static class ImageCacheMemory extends BarryCacheMemory<String,Bitmap> {
        @Override
        protected int sizeOf(String s, Bitmap bm) {
            return bm.getByteCount();
        }
    }
}
