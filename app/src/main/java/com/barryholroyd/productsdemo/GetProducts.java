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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.barryholroyd.productsdemo.ActivityProductList.trace;

/**
 * TBD:
 */
public class GetProducts
{
	/**
	 * Singleton.
	 * <p>
	 *     This was made a singleton because we only need a single instance of it
	 *     and it makes it easier to access from anywhere in the app. This singleton
     *     exists independently of any particular Activity instance.
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

	/** API key. */
	static final private String API_KEY = "vwvv4uds6hqr3q8vr6qgrn8v";

    /** API Base Url */
    static final private String API_BASE_URL = "http://api.walmartlabs.com";

    /** API Version */
    static final private String API_VERSION = "v1";

    /** API Paginated Items */
    static final private String API_PAGINATED_ITEMS = "paginated/items";

    /** API Format */
    static final private String API_FORMAT = "json";

    /**
     * Category to use.
     * This is a hardcoded category to use to drive the demo.
     * The full set of available categories can be found at
     * <a href="http://api.walmartlabs.com/v1/taxonomy?apiKey={apiKey}">categories</a>.
     */
    static final private int API_CATEGORY_ELECTRONICS = 3944;
    /**
     * Valid JSON tokens for paginated products.
     *
     * @see <a href="https://developer.walmartlabs.com/docs/read/Paginated_Products_API">
     *     Paginated Products API</a>
     */
    static private List<String> JSON_PAGINATED_PRODUCTS = Arrays.asList(
            "category", "format", "nextPage", "items"
    );

    /**
     * Valid JSON tokens for product information.
     *
     * @see <a href="https://developer.walmartlabs.com/docs/read/Item_Field_Description">
     *     Item Response Groups</a>
     */
    static private List<String> JSON_ITEM_INFO = Arrays.asList(
			"addToCartUrl", "affiliateAddToCartUrl", "age",
			"attributes", "availableOnline", "bestMarketplacePrice",
			"brandName", "bundle", "categoryNode", "categoryPath",
			"clearance", "color", "customerRating", "customerRatingImage",
			"freeShippingOver50Dollars", "freeShipToStore", "gender",
			"isbn", "itemId", "largeImage", "longDescription",
			"marketplace", "maxItemsInOrder", "mediumImage", "modelNumber",
			"msrp", "name", "ninetySevenCentShipping", "numReviews",
			"offerType", "overnightShippingRate", "parentItemId", "preOrder",
			"preOrderShipsOn", "productTrackingUrl", "productUrl", "rollBack",
			"salePrice", "sellerInfo", "shipToStore", "shippingPassEligible",
			"shortDescription", "size", "specialBuy", "standardShipRate",
			"stock", "thumbnailImage", "twoThreeDayShippingRate", "upc",
			"variants"
	);

	/**
	 * The total number of products downloaded so far.
	 */
	private int totalDownloaded = 0; // DEL:

    private String url_next_batch = null;

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
            instance.url_next_batch = createUrlInitial(API_CATEGORY_ELECTRONICS);
//			instance.totalDownloaded = 0; DEL:
		}
	}

	/** Create initial paginated products list request. */
    static private String createUrlInitial(int category) {
        return String.format("%s/%s/%s?category=%d&apiKey=%s&format=%s",
                API_BASE_URL, API_VERSION, API_PAGINATED_ITEMS,
                category, API_KEY, API_FORMAT);
    }

	/** Create "new page" paginated products list request. */
	static private String createUrlNextPage(String nextPage) {
		return String.format("%s%s", API_BASE_URL, nextPage);
	}

	/**
	 * Get the next batch of products and display them.
	 * <p>
	 *    This method is synchronized so that any previous calls to it
	 *    complete first. This will the run smoothly, based on information updated
	 *    from the previous run(s).
	 *
     * @param a
     */
    public synchronized void getProductBatch(Activity a) {
		if (!checkNetworkConnectivity(a)) {
            Support.loge("No network connection.");
		}
		else {
			new DownloadJsonTask(a).execute(url_next_batch);
		}
	}

	private boolean checkNetworkConnectivity(Activity a) {
		ConnectivityManager cm = (ConnectivityManager)
			a.getSystemService(a.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}

    /**
     * Download the JSON, update the list of products and display it.
     */
	private class DownloadJsonTask extends AsyncTask<String, Void, ProductInfoArrayList>
	{
        /**
         * A WeakReference to the main activity.
         * <p>
         *     This is only used for Toaster.display(), but it is necessary so that
         *     Toaster.display() doesn't crash if the Activity is gone.
         */
        private WeakReference<Activity> wrActivity;

        /** Retain Activity on a hook only for the toaster display. */
        Activity a;

        DownloadJsonTask(Activity a) {
            super();
			wrActivity = new WeakReference<>(a);
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
                Toaster.display(wrActivity, msg); // TBD: can crash if Activity is gone.
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
                if (JSON_PAGINATED_PRODUCTS.contains(name)) {
                    switch (name) {
                        case "nextPage":
							String nextPage = reader.nextString();
							if (nextPage == null) {
								Support.logd("DONE"); // TBD:
								System.exit(9); // TBD:
							}
                            to.urlNextPage = createUrlNextPage(nextPage);
                            break;
                        case "items":
                            to.pial = readProducts(reader);
                            break;
						default:
							reader.skipValue();
							break;
                    }
                }
                else {
                    Support.loge("Error: bad token in JSON stream: " + name);
                    reader.skipValue();
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
                if (JSON_ITEM_INFO.contains(name)) {
                    switch (name) {
                        case "itemId":
                            pi.id = reader.nextInt();
                            break;
                        case "name":
                            pi.name = Support.htmlToText(reader.nextString());
                            break;
                        case "msrp":
                            pi.price = reader.nextDouble();
                            break;
                        case "shortDescription":
                            pi.shortDescription = Support.htmlToText(reader.nextString());
                            break;
                        case "longDescription":
                            pi.longDescription = Support.htmlToText(reader.nextString());
                            break;
                        case "thumbnailImage":
                            pi.imageUrl = reader.nextString();
                            break;
                        case "stock":
                            pi.inStock = reader.nextString();
                            break;
                        case "customerRating":
                            pi.reviewRating = reader.nextString();
                            break;
                        case "numReviews":
                            pi.reviewCount = reader.nextInt();
                            break;
						default:
							reader.skipValue();
							break;
					}
				}
				else {
					Support.loge("Error: bad token in JSON stream: " + name);
					reader.skipValue();
				}
			}
			reader.endObject();

			return pi;
		}

        // DEL:
		private void otherToken(JsonReader reader, String name) throws IOException {
            Support.loge("Error: bad token in JSON stream: " + name);
			reader.skipValue();
		}
	}

	private class TopObject
	{
		String urlNextPage;
		ProductInfoArrayList pial;
	}
}
