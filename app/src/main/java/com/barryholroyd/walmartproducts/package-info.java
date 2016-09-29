/**
 * WalmartProducts is a high-performance demo app which lists a sample
 * of products from Walmart.
 * <p>
 * The app is provided for demonstration purposes only -- the products
 * listed are not real products and are not intended to reflect actual
 * products from Walmart. All product information is downloaded as JSON
 * data from a demo server.
 * <p>
 * The app is written to provide smooth scrolling and optimize memory
 * and disk usage.
 * <ul>
 *   <li>Pre-fetching of product descriptions in batches, on demand.
 *   <li>Images downloaded separately, in the background.
 *   <li>Configurable memory cache.
 *   <li>Configurable disk cache.
 * </ul>
 * There are two separate implementations for downloading images in the
 * background: Threads-based and AsyncTask-based. The two are equivalent --
 * they are both provided only for demonstration purposes.
 * <p> 
 * There is a well-known issue wherein images downloaded in the background
 * may arrive too late to be used for their product list entries because
 * the relevant product entry is no longer valid -- specifically, in this
 * implementation, the RecyclerView ViewHolder may have been recycled by
 * the time the image arrives. This implementation recognizes and deals
 * with that situation in both the Threads and AsyncTask approaches.
 * <p>
 * See {@link com.barryholroyd.walmartproducts.Configure} for memory cache
 * and disk cache configuration parameters.
 *
 * @author Barry Holroyd
 */
package com.barryholroyd.walmartproducts;
