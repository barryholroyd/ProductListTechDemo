package com.barryholroyd.walmartproducts;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import java.io.InputStream;

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
     * If true, use threads to handled background loading; otherwise, use AsyncTask.
     */
    private static final boolean USE_THREADS = true;

    static void load(Context ctx, final ImageView iv, final String url) {
        Bitmap bitmap =  ImageCacheMemory.get(iv, url);
        if (bitmap != null) {
            iv.setImageBitmap(bitmap);
        }
        else {
            if (USE_THREADS)    ImageLoaderAsyncTask.load(iv, url);
            else                ImageLoaderThread.load(iv, url);
        }
    }

    class ImageCacheMemory extends BarryCacheMemory<String,Bitmap> {
        @Override
        protected int sizeOf(String s, Bitmap bm) {
            return bm.getByteCount();
        }
    }
}
