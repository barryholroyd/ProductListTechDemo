package com.barryholroyd.productsdemo;

/**
 * Runtime exception for the BlobCacheMemory module.
 */
class ImageDiskCacheException extends RuntimeException {
    ImageDiskCacheException(String s) {
        super(s);
    }
}
