package com.barryholroyd.walmartproducts;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Threads-based approach to loading images. Check the disk cache first; if that
 * doesn't work, then pull the image from the network.
 */

public class ImageLoaderThreads {
    static void load(ImageView iv, String url, ImageCacheMemory cacheMemory) {
        Bitmap bitmap = (Bitmap) BarryCacheDisk.get(url);
    }
}
