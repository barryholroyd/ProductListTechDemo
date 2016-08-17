package com.barryholroyd.walmartproducts;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
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
			if (is == null)
				return null;
			return parseJsonStream(is);
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

		/**
		 * Parse the JSON stream from the network into an array of ProductInfo objects.
		 *
		 * @param is the input stream.
		 * @return   the list of ProductInfo objects.
		 */
		private ProductInfoArrayList parseJsonStream(InputStream is) {
			JsonReader jr = new JsonReader(is);


			return jr.parse();
		}
	}

	private class JsonReader
	{
		private InputStream is = null;

		JsonReader(InputStream _is) {
			is = _is;
		}

		ProductInfoArrayList parse() {
			ProductInfoArrayList pial = new ProductInfoArrayList();
			// TBD: parse everything
			return pial;
		}
	}
}
