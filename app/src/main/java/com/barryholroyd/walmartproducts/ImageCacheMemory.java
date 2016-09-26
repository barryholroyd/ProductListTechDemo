package com.barryholroyd.walmartproducts;

import android.graphics.Bitmap;

/**
 * TBD:
 */
class ImageCacheMemory extends BarryCacheMemory<String, Bitmap> {
    @Override
    protected int sizeOf(String s, Bitmap bm) {
        return bm.getByteCount();
    }
}
