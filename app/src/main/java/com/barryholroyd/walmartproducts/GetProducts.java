package com.barryholroyd.walmartproducts;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import org.json.simple.JSONArray;

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

	public ProductInfoArrayList getNextBatch() {
		if ((maxProducts != 0) && (page * batchSize > maxProducts)) {
			return null;
		}
		return getProducts(page++, batchSize);
	}

	/**
	 * Get the next batch of products.
	 */
	private ProductInfoArrayList getProducts(int page, int count) {
		JSONArray products =
			if (products == null)

				return null;
	}

	private JSONArray getListOfProducts(int page, int count) {
		String urlString = String.format(
			"%s%s/%d/%d", API_PREFIX, API_KEY, page, count);
		String jsonString = null;
			jsonString = getJsonString(urlString);

		return convertToJsonArray(jsonString);
	}

	private boolean checkNetworkConnectivity() {
		Activity a = Support.getActivity();
		ConnectivityManager cm = (ConnectivityManager)
			a.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}

	private String getJsonString(String urlString) {
		if (!checkNetworkConnectivity()) {
			Support.loge("No network connection.");
			return null;
		}

		new DownloadJsonTask().execute(urlString);

	}

	private class DownloadJsonTask extends AsyncTask<String, Void, String>
	{
		@Override
		protected String doInBackground(String urls[]) {
			return download(urls[0]);
		}

		/**
		 * Process downloaded JSON string.
		 *
		 * @param result the downloaded JSON string, as returned by doInBackground().
		 */
		@Override
		protected void onPostExecute(String result) {
			if (result == null)
				return;
			// TBD.
		}

		/**
		 * Download the JSON string from the network.
		 */
		private String download(String urlString) {
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
				InputStream is = c.getInputStream();
				return getString(is);
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

		private String
		ProductInfoArrayList pial = new ProductInfoArrayList();

	}
}
