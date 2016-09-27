package com.barryholroyd.walmartproducts;

import android.graphics.Bitmap;

/**
 * Memory cache system extending BlobCacheMemory to be specific to a String
 * (url) for the key and a Bitmap for the blob. sizeOf() is overridden so that
 * we can specify the maximum amount of storage allocated using bytes for the
 * unit size.
 */
class ImageCacheMemory extends BlobCacheMemory<String, Bitmap> {
    @Override
    protected int sizeOf(String s, Bitmap bm) {
        return bm.getByteCount();
    }
}
