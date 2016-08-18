package com.barryholroyd.walmartproducts;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

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
	 * The "page" of the next set of "batchSize" products to get.
	 */
	static private int page = 1;
	/**
	 * Number of products to request in a single batch. Maximum is 30.
	 */
	static final private int batchSize = 10;
	/**
	 * API key.
	 * This was sent to me by Walmart for the test taken in August, 2016.
	 */
	static final private String API_KEY = "cec8e676-d56f-49b9-987a-989a9d23a724";
	/**
	 * URL Prefix.
	 * Full URL is: API_PREFIX + API_KEY + "/<page>/<batchSize>"
	 */
	static final private String API_PREFIX =
		"https://walmartlabs-test.appspot.com/_ah/api/walmart/v1/walmartproducts/";

	/**
	 * The total number of products available.
	 * This is included in the response to each request.
	 */
	static private int maxProducts = 0;

	public void reset() { page = 1; }

	public void getNextBatch() {
		if ((maxProducts == 0) || (page * batchSize <= maxProducts)) {
			getProducts(page++, batchSize);
		}
	}

	/**
	 * Get the next batch of products.
	 */
	private void getProducts(int page, int count) {
		String urlString = String.format(
			"%s%s/%d/%d", API_PREFIX, API_KEY, page, count);
		if (!checkNetworkConnectivity()) {
			Support.loge("No network connection.");
		}
		else {
			new DownloadJsonTask().execute(urlString);
		}
	}

	private boolean checkNetworkConnectivity() {
		Activity a = Support.getActivity();
		ConnectivityManager cm = (ConnectivityManager)
			a.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}

	private class DownloadJsonTask extends AsyncTask<String, Void, ProductInfoArrayList>
	{
		@Override
		protected ProductInfoArrayList doInBackground(String urls[]) {
			InputStream is = getInputStream(urls[0]);
			WmpJsonReader jr = new WmpJsonReader(is);
			return jr.parse();
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
			RecyclerView rv = ActivityProductList.mRecyclerView;
			ProductListRecyclerAdapter plra =
				(ProductListRecyclerAdapter) rv.getAdapter();
			plra.updateData(pial);
		}

		/**
		 * Download the JSON string from the network.
		 */
		private InputStream getInputStream(String urlString) {
			try {
				URL url = new URL(urlString);
				HttpURLConnection c = (HttpURLConnection) url.openConnection();
				c.setReadTimeout(10000); // ms
				c.setConnectTimeout(15000); // ms
				c.setRequestMethod("GET");
				c.setDoInput(true);
				c.connect();
				int response = c.getResponseCode();
				Support.logd(String.format("Http response code: %d.", response));
				return c.getInputStream();
			}
			catch (MalformedURLException e) {
				Support.loge(String.format("Malformed url: %s", urlString));
				return null;
			}
			catch (IOException e) {
				Log.e("getListOfProducts", "IO Exception", e);
				return null;
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
			JsonReader jr = null;
			try {
				jr = new JsonReader(new InputStreamReader(is, "UTF-8"));
				return readProductsArray(jr);
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

		ProductInfoArrayList readProductsArray(JsonReader reader) throws IOException
		{
			TopObject to = readTopObject(reader);
			return to.pial;
		}

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
					default:	badToken(reader);
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
					case "productId":	pi.id	= reader.nextString();	break;
					case "productName":	pi.name	= reader.nextString();	break;
					case "shortDescription":	pi.shortDescription	= reader.nextString();	break;
					case "longDescription":	pi.longDescription	= reader.nextString();	break;
					case "price":	pi.price	= reader.nextString();	break;
					case "productImage":	pi.imageUrl	= reader.nextString();	break;
					case "reviewRating":	pi.reviewRating	= reader.nextDouble();	break;
					case "reviewCount":	pi.reviewCount	= reader.nextInt();	break;
					case "inStock":	pi.inStock	= reader.nextBoolean();	break;
					default:	badToken(reader);
				}
			}
			reader.endObject();

			return pi;
		}

		private void badToken(JsonReader reader) throws IOException {
			Support.loge("Error: bad token in JSON stream.");
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
