package com.barryholroyd.productsdemo.product_info;

import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.util.JsonToken;

import com.barryholroyd.productsdemo.ActivityProductList;
import com.barryholroyd.productsdemo.network.NetworkSupport;
import com.barryholroyd.productsdemo.network.NetworkSupportException;
import com.barryholroyd.productsdemo.support.Support;
import com.barryholroyd.productsdemo.support.Toaster;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

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
     *
     * This is a hardcoded category to use to drive the demo.
     * The full set of available categories can be found at
     * <a href="http://api.walmartlabs.com/v1/taxonomy?apiKey={apiKey}">categories</a>.
     * <p>
     *     When there are no more items, the nextPage JSON field will still have a
     *     URL in it, but the returned JSON will simply be an empty object: {}.
     */
     static final private String API_CATEGORY_AVENGERS_BOOKS = "1085632_1229464_1229469"; // 21 items
     static final private String API_CATEGORY_ELECTRONICS = "3944"; // many items
     static final private String API_CATEGORY = API_CATEGORY_ELECTRONICS;

    /**
     * Valid JSON tokens for paginated products.
     *
     * @see <a href="https://developer.walmartlabs.com/docs/read/Paginated_Products_API">
     *     Paginated Products API</a>
     */
    static final private List<String> JSON_PAGINATED_PRODUCTS = Arrays.asList(
            "category", "format", "nextPage", "items"
    );

    /**
     * Valid JSON tokens for product information.
     *
     * @see <a href="https://developer.walmartlabs.com/docs/read/Item_Field_Description">
     *     Item Response Groups</a>
     */
    static final private List<String> JSON_ITEM_INFO = Arrays.asList(
			"addToCartUrl", "affiliateAddToCartUrl", "age",
			"attributes", "availableOnline", "bestMarketplacePrice",
			"brandName", "bundle", "categoryNode", "categoryPath",
			"clearance", "color", "customerRating", "customerRatingImage",
			"freeShippingOver50Dollars", "freeShipToStore","freight", "gender",
			"isbn", "itemId", "largeImage", "longDescription",
			"marketplace", "maxId", "maxItemsInOrder", "mediumImage", "modelNumber",
			"msrp", "name", "ninetySevenCentShipping", "numReviews",
			"offerType", "overnightShippingRate", "parentItemId", "preOrder",
			"preOrderShipsOn", "productTrackingUrl", "productUrl", "rollBack",
			"salePrice", "sellerInfo", "shipToStore", "shippingPassEligible",
			"shortDescription", "size", "specialBuy", "standardShipRate",
			"stock", "thumbnailImage", "twoThreeDayShippingRate", "upc",
			"variants"
	);

	/**
	 * Url of the next batch of items to get.
	 * <p>
	 *     When there are no more items to return, this will still be a valid URL
	 *     but using it will return an empty JSON object: {}.
	 */
    private String urlNextPage = null;

	/** allItemsRead becomes "true" when all the items have been downloaded. */
	private boolean allItemsRead = false;

    /** Getter for allItemsRead. */
    public boolean areAllItemsRead() { return allItemsRead; }

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
	static public void reset() {
		if (instance != null) {
			instance.allItemsRead = false;
            instance.urlNextPage = createUrlInitial(API_CATEGORY);
		}
	}

	/** Create initial paginated products list request. */
    static public String createUrlInitial(String category) {
        return String.format("%s/%s/%s?category=%s&apiKey=%s&format=%s",
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
			new DownloadJsonTask(a).execute(urlNextPage);
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
                Toaster.display(wrActivity, msg);
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
			RecyclerView rv = ActivityProductList.getRecyclerView();
			ProductListRecyclerAdapter plra =
				(ProductListRecyclerAdapter) rv.getAdapter();
			plra.updateData(pial);
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
				TopObject to = parseJsonData(jr);
				if (to != null)	return to.pial;
				else			return null;
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
		 * Decode the product info returned from the cloud. It contains some general information
		 * as well as the set of products included in the next "batch".
		 *
		 * @param reader    JSON reader containing the JSON description of the products.
		 * @return          returns the ArrayList of products, with each product represented
		 *                  by a ProductInfo instance. If there are no more products to be
		 *                  read, returns null.
		 * @throws IOException IOException can be thrown by the JsonReader instance.
		 */
		private TopObject parseJsonData(JsonReader reader) throws IOException {
			TopObject to = new TopObject();

			reader.beginObject();
			if (!reader.hasNext()) {
				allItemsRead = true;
				return null;
			}
			while (reader.hasNext()) {
                String name = reader.nextName();
                if (JSON_PAGINATED_PRODUCTS.contains(name)) {
                    switch (name) {
                        case "nextPage":
							String nextPage = reader.nextString();
                            to.urlNextPage = createUrlNextPage(nextPage);
                            urlNextPage = to.urlNextPage;
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
                            pi.setId(reader.nextInt());
                            break;
                        case "name":
                            pi.setName(Support.htmlToText(reader.nextString()));
                            break;
                        case "msrp":
                            pi.setPrice(reader.nextDouble());
                            break;
                        case "shortDescription":
                            pi.setShortDescription(Support.htmlToText(reader.nextString()));
                            break;
                        case "longDescription":
                            pi.setLongDescription(Support.htmlToText(reader.nextString()));
                            break;
                        case "thumbnailImage":
                            pi.setImageUrl(reader.nextString());
                            break;
                        case "stock":
                            pi.setInStock(reader.nextString());
                            break;
                        case "customerRating":
                            pi.setReviewRating(reader.nextString());
                            break;
                        case "numReviews":
                            pi.setNumReviews(reader.nextInt());
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
	}

    /**
     * Object to contain the top-level parsed JSON. Currently, there isn't
     * much need for this because we only use the ProductInfoArrayList, but
     * it's structurally nice to have in place in case we want to do something
     * with the non-list data in the future.
     */
	private class TopObject
	{
		String urlNextPage;
		ProductInfoArrayList pial;
	}
}
