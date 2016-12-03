package com.barryholroyd.productsdemo;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.util.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import static com.barryholroyd.productsdemo.ActivityProductList.trace;

/**
 * Get a list of products.
 *
 * @author Barry Holroyd
 * @see    <a href="https://walmartlabs-test.appspot.com">Walmart Products API (mock)</a>
 * @see    <a href="https://walmartlabs-test.appspot.com/_ah/api/walmart/v1">Documentation</a>
 */
public class GetProducts
{
	/**
	 * Singleton.
	 * <p>
	 *     This was made a singleton because we only need a single instance of it
	 *     and it makes it easier to access from anywhere in the app.
	 */
	static public final GetProducts instance = new GetProducts();

	/**
	 * Private constructor to prevent external instantiation. The check for null isn't
	 * strictly necessary, but theoretically prevents someone from using reflection
	 * and Constructor.setAccessible() from creating one, or from one being created internally
	 * (within this class) by mistake.
	 */
	private GetProducts() {
		if (instance != null)
			throw new IllegalStateException("Only a single instance of GetProducts is allowed");
	}

	/**
	 * Number of products to request in a single pageNumber. Maximum is 30.
	 */
	static final private int PAGE_SIZE = 25;

	/**
	 * API key.
	 * This was sent to me by Walmart for the test taken in August, 2016.
	 */
	static final private String API_KEY = "cec8e676-d56f-49b9-987a-989a9d23a724";

	/**
	 * URL Prefix.
	 * Full URL is: API_PREFIX + API_KEY + "/&lt;pageNumber&gt;/&lt;batchSize&gt;"
	 */
	static final private String API_PREFIX =
		"https://walmartlabs-test.appspot.com/_ah/api/walmart/v1/walmartproducts/";

	/**
	 * The "pageNumber" of the next set of "batchSize" products to get.
	 */
	private int pageNumber = 1;

	/**
	 * The total number of products available.
	 * This is included in the response to each request.
	 */
	private int maxProducts = 0;

	/**
	 * The total number of products downloaded so far.
	 */
	private int totalDownloaded = 0;

	int getMaxProducts() { return maxProducts; }

	/**
	 * Reset the instance data for this (static) singleton.
	 * <p>
	 * Because this class is a singleton, the reference for it is hung on a static variable.
	 * This can cause problems when the "Back" key is used.
	 * <p>
	 * When the user hits the "Back" key and there is only a single Activity present, that
	 * final Activity is destroyed and the app is moved to the background. The system assumes
	 * that the app is effectively being exited and so doesn't call onSaveInstanceState().
	 * However, the app's process is not actually killed, so the app's static data is retained.
	 * In addition, the app remains accessible on the Overview screen so the user can easily
	 * restart it.
	 * <p>
	 * When that happens, the app needs to understand whether or not its static data has
	 * already been initialized and, if necessary, reset it.
	 */
	static void reset() {
		if (instance != null) {
			instance.pageNumber = 1;
			instance.maxProducts = 0;
			instance.totalDownloaded = 0;
		}
	}

	/**
	 * Get the next batch of products to be displayed.
	 * <p>
	 *    This method is synchronized so that any previous calls to getNextPage()
	 *    complete first. This will the run smoothly, based on information updated
	 *    from the previous run(s).
	 * <p>
	 *     There appears to be a bug in the server. If you request a full page of products
	 *     that extends beyond the end of the list, it will actually return a full page.
	 *     Interestingly, that doesn't seem to be the case if you use a page size of 1.
	 *     In any case, when we get to the end of the list we only ask for the number of
	 *     remaining products if there aren't enough left to fill a full page.
     * <p>
     *     NTH: Ideally, we should provide a method for checking to see if the total
     *     number of available products has changed.
	 */
	public synchronized void getNextPage(Activity a) {
        // If all products have already been downloaded, just return without doing anything.
        if ((maxProducts != 0) && (totalDownloaded == maxProducts)) {
            return;
        }

        /*
         * Set the page size. Handle the case where only a part of a page (at the end of
         * the product set) needs to be downloaded). Note that we already know that there
         * is are one or more products that have not yet been downloaded.
         */
		int pageSize = PAGE_SIZE;
		if ((maxProducts != 0) && (pageNumber * PAGE_SIZE > maxProducts)) {
			pageSize = maxProducts - ((pageNumber-1) * PAGE_SIZE);
		}
		getProducts(a, pageNumber++, pageSize);
	}

	/**
	 * Get the next batch of products.
	 *
     * @param a
     * @param batch The batch to be downloaded.
     * @param count the number of items in the batch to be downloaded.
     */
	private void getProducts(Activity a, int batch, int count) {
		String urlString = makeUrl(batch, count);

		if (!checkNetworkConnectivity(a)) {
            Support.loge("No network connection.");
		}
		else {
			new DownloadJsonTask(a).execute(urlString);
		}
	}

	private boolean checkNetworkConnectivity(Activity a) {
		ConnectivityManager cm = (ConnectivityManager)
			a.getSystemService(a.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}

	/**
	 * Make a URL to get the next batch of products. They will be returned in JSON format.
	 * <p>
	 *     This is public so that unit tests can access it.
	 *
	 * @param batch	The number of the batch to load.
	 * @param count The number of products requested in the batch.
     * @return The url that can be used to make the request.
     */
	static public String makeUrl(int batch, int count) {
		return String.format(
				"%s%s/%d/%d", API_PREFIX, API_KEY, batch, count);
	}

	private class DownloadJsonTask extends AsyncTask<String, Void, ProductInfoArrayList>
	{
        Activity a;

        DownloadJsonTask(Activity _a) {
            super();
			a = _a;
        }
		@Override
		protected ProductInfoArrayList doInBackground(String urls[]) {
            // Download the JSON string from the network.
			try (InputStream is = NetworkSupport.getInputStreamFromUrl(urls[0])) {
				WmpJsonReader jr = new WmpJsonReader(is);
				return jr.parse();
			}
			catch (NetworkSupportException | IOException e) {
				String msg = String.format(String.format("GetProducts: %s", e.getMessage()));
				Support.loge(msg);
                Toaster.display(a, msg);
                return null;
			}
		}

		/**
		 * Add the list of products to the list backing array and display it.
		 *
		 * @param pial the list of products, as returned by doInBackground().
		 */
		@Override
		protected void onPostExecute(ProductInfoArrayList pial) {
			if (pial == null)
				return;
			RecyclerView rv = ActivityProductList.recyclerView;
			ProductListRecyclerAdapter plra =
				(ProductListRecyclerAdapter) rv.getAdapter();
			plra.updateData(pial);
            totalDownloaded += pial.size();

            // Sanity check. Subtract 1 because of the header added locally.
			int itemCount = plra.getItemCount() - 1;
            if (totalDownloaded != itemCount) {
				String msg = String.format(
						"Total downloaded count corrupted (totalDownloaded=%d itemCount=%d",
						totalDownloaded, itemCount);
                throw new IllegalStateException(msg);
            }
		}
	}

	private class WmpJsonReader
	{
		private InputStream is = null;

		/**
		 * Constructor.
		 *
		 * @param _is the JSON input stream.
		 */
		WmpJsonReader(InputStream _is) {
			is = _is;
		}

		/**
		 * Parse the JSON stream from the network into an array of ProductInfo objects.
		 *
		 * @return   the list of ProductInfo objects.
		 */
		ProductInfoArrayList parse() {
			if (is == null)
				return null;
			JsonReader jr;
			try {
				jr = new JsonReader(new InputStreamReader(is, "UTF-8"));
				return readProductsInfo(jr);
			}
			catch (UnsupportedEncodingException e) {
                Support.loge("Error: problem parsing network data - unsupported coding exception.");
				return null;
			}
			catch (IOException e) {
                Support.loge("Error: problem parsing network data - io exception.");
				return null;
			}
		}

		/**
		 * Process the product info returned from the cloud. Specially, break out any generic
		 * information from the array of products.
		 *
		 * @param reader    JSON reader containing the JSON description of the products.
		 * @return          returns the ArrayList of products, with each product represented
		 *                  by a ProductInfo instance.
		 * @throws IOException IOException can be thrown by the JsonReader instance.
		 */
		ProductInfoArrayList readProductsInfo(JsonReader reader) throws IOException
		{
			TopObject to = readTopObject(reader);
			maxProducts = to.totalProducts;
			return to.pial;
		}

		/**
		 * Decode the product info returned from the cloud. It contains some general information
		 * as well as the set of products included in the next "batch".
		 *
		 * @param reader    JSON reader containing the JSON description of the products.
		 * @return          returns the ArrayList of products, with each product represented
		 *                  by a ProductInfo instance.
		 * @throws IOException IOException can be thrown by the JsonReader instance.
		 */
		private TopObject readTopObject(JsonReader reader) throws IOException {
			TopObject to = new TopObject();

			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				switch (name) {
					case "id":	to.id = reader.nextString();	break;
					case "products":	to.pial = readProducts(reader);	break;
					case "totalProducts":	to.totalProducts = reader.nextInt();	break;
					case "pageNumber":	to.pageNumber = reader.nextInt();	break;
					case "pageSize":	to.pageSize = reader.nextInt();	break;
					case "status":	to.status = reader.nextInt();	break;
					case "kind":	to.kind = reader.nextString();	break;
					case "etag":	to.etag = reader.nextString();	break;
					default:	badToken(reader, name);
				}
			}
			reader.endObject();

			return to;
		}

		private ProductInfoArrayList readProducts(JsonReader reader) throws IOException {
			if (reader.peek() == JsonToken.NULL)
				return null;

			ProductInfoArrayList pial = new ProductInfoArrayList();

			reader.beginArray();
			while (reader.hasNext()) {
				ProductInfo pi = readProductInfo(reader);
				pial.add(pi);
			}
			reader.endArray();

			return pial;
		}

		private ProductInfo readProductInfo(JsonReader reader) throws IOException {
			ProductInfo pi = new ProductInfo();

			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				switch (name) {
					case "productId":		 pi.id	= reader.nextString();					break;
					case "productName":		 pi.name	=
                            Support.htmlToText(reader.nextString());	break;
					case "shortDescription": pi.shortDescription	=
                            Support.htmlToText(reader.nextString());	break;
					case "longDescription":	 pi.longDescription	=
                            Support.htmlToText(reader.nextString());	break;
					case "price":			 pi.price	= reader.nextString();				break;
					case "productImage":	 pi.imageUrl	= reader.nextString();			break;
					case "reviewRating":	 pi.reviewRating	= reader.nextDouble();		break;
					case "reviewCount":		 pi.reviewCount	= reader.nextInt();				break;
					case "inStock":			 pi.inStock	= reader.nextBoolean();				break;
					default:				 badToken(reader, name);
				}
			}
			reader.endObject();

			return pi;
		}

		private void badToken(JsonReader reader, String name) throws IOException {
            Support.loge("Error: bad token in JSON stream: " + name);
			reader.skipValue();
		}
	}

	private class TopObject
	{
		String id;
		ProductInfoArrayList pial;
		int totalProducts;
		int pageNumber;
		int pageSize;
		int status;
		String kind;
		String etag;
	}
}
