package com.barryholroyd.productsdemo;

/**
 * Runtime exception for the CacheMemoryBlob module.
 */
class ImageDiskCacheException extends RuntimeException {
    ImageDiskCacheException(String s) {
        super(s);
    }
}
