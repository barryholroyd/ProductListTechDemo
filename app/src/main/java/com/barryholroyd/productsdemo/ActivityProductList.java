package com.barryholroyd.productsdemo;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.barryholroyd.productsdemo.product_info.GetProducts;
import com.barryholroyd.productsdemo.product_info.ProductInfo;
import com.barryholroyd.productsdemo.product_info.ProductInfoArrayList;
import com.barryholroyd.productsdemo.product_info.ProductListOnScrollListener;
import com.barryholroyd.productsdemo.product_info.ProductListRecyclerAdapter;
import com.barryholroyd.productsdemo.support.ActivityPrintStates;
import com.barryholroyd.productsdemo.support.Configure;
import com.barryholroyd.productsdemo.support.Support;

/*
 * TODO: rename app to WalmartProducts
 * TODO: Add Preferences.
 * TODO: Are images ever *dropped*, such that they have to be reloaded? Verify.
 * TODO: Finish comments.
 * TODO: Run code check tools.
 */

/**
 * TBD: Where should this go?
 *
 * High performance demo for listing products from Walmart.
 * <p>
 * This app demos how a list of items containing images (e.g., a list of products with
 * associated thumbnail images) can be downloaded from the web with minimal delay. It specifically
 * is <i>not</i> intended to be a full, user-facing app. It's functionality is limited to
 * displaying all of the products in Walmart's top-level <i>Electronics</i> category. It could
 * be easily expanded to provide full listings from any/all of Walmart's approximately
 * 2500 categories and/or an extensive list of additional capabilities.
 * <p>
 * The primary challenge is ensuring that the delayed loading of images doesn't not cause
 * inconsistencies (wrong image) when RecyclerView has reallocated the ViewHolder to another
 * row by the time the requested image arrives and is available.
 * <p>
 * In addition to that, several mechanisms are included to provide the smoothest experience
 * for the user.
 *     <ul>
 *         <li> Images downloaded in the background. Two implementations are provided:
 *              AsnycTask-based and threads-based.
 *         <li> Initial image url retained as a field and compared to the (potentially updated)
 *              URL of the ViewHolder upon image arrival.
 *         <li> Configurable look-ahead pre-loading.
 *         <li> Configurable memory cache.
 *         <li> Configurable disk cache.
 *     </ul>
 * <p>
 * The <a href="Walmart Open API">https://developer.walmartlabs.com/</a> is used.
 * This app accesses the paginated products portion of that API. 100 products are
 * returned in each batch and each batch contains the URL for the next batch.
 *
 * @author Barry Holroyd
 * @see    <a href="https://developer.walmartlabs.com/">Walmart Open API</a>
 * @see    <a href="https://developer.walmartlabs.com/docs/read/Paginated_Products_API">
 *            Paginated Products</a>
 */

/**
 * This is the main activity -- it lists the products.
 * <p>
 * Clicking on a product will call ActivityProductInfo to display product-specific information.
 *
 * @author Barry Holroyd
 * @see    <a href="https://walmartlabs-test.appspot.com">Walmart Products API (mock)</a>
 * @see    <a href="https://walmartlabs-test.appspot.com/_ah/api/walmart/v1">Documentation</a>
 */
public class ActivityProductList extends ActivityPrintStates
{
	/**
	 * Logger tag for this application.
	 */
	static final public String LOGTAG = "ProductDemo";
	/**
	 * RecyclerView used to display the list of products pulled from the cloud.
	 */
	static RecyclerView recyclerView;

	/**
	 * Key for storing an instance of the ProductInfoArrayList object. Get
	 * it with getParcelableArrayList().
	 */
	static final String PIAL = "PIAL";

	/** Getter for recyclerView. */
	static final public RecyclerView getRecyclerView() { return recyclerView; }

    /**
	 * Standard onCreate method.
	 *
	 * @param savedInstanceState used to transfer the backing array for the products
	 *                           across device rotations.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.productlist);

		initRecyclerView();

		/*
		 * If savedInstance is null, then we are either starting the app for the first time
		 * or are restarting the app after (for example), the "Back" button has been used.
		 * In the latter case, we have to make sure that the GetProducts singleton is
		 * re-initialized to match the rest of the app (by essentially resetting it to be
		 * uninitialized).
		 */
		if (savedInstanceState == null) {
			GetProducts.reset();
			GetProducts.instance.getProductBatch(this);
		}
		else {
			// Display the product list on device reconfiguration.
            refreshListDisplay(savedInstanceState);
		}
	}

	/**
	 * Display the existing product list (e.g., after a device rotation).
	 * <p>
	 * There are a number of interesting issues with casting here.
	 * <p>
	 * This works:
	 *   <code>
	 *   ArrayList<ProductInfo> pial = savedInstanceState.getParcelableArrayList(PIAL);
	 *   ProductInfoArrayList pial2 = (ProductInfoArrayList) pial;
	 *   </code>
	 * Reason: getParcelableArrayList() returns an ArrayList{@literal <T extends Parcelable>}.
	 * Since ProductInfo extends Parcelable, it is a valid Target Type for T.
	 * Since ProductInfoArrayList extends ArrayList{@literal <ProductInfo>}, pial can
	 * be assigned to pail2. However, since this is a downcast (pial could actually
	 * be some other subclass of ArrayList{@literal <ProductInfo>}), an explicit cast has to
	 * be used.
	 * <p>
	 * This fails:
	 *   <code>
	 *   ProductInfoArrayList pial = ((ArrayList<ProductInfo>)
	 *     savedInstanceState.getParcelableArrayList(PIAL));
	 *   </code>
	 * Reason: ArrayList{@literal <ProductInfo>} is not a subclass of ArrayList{@literal <Parcelable>}, or
	 * vice-versa, even though ProductInfo is a subclass of Parcelable.
	 * <p>
	 * For the following, see:
	 *   http://docs.oracle.com/javase/tutorial/java/generics/genTypeInference.html,
	 *   the discussion of processStringList().
	 * <p>
	 * This fails:
	 *   <code>
	 *   ArrayList<ProductInfo> pialBad =
	 *     (ArrayList<ProductInfo>) savedInstanceState.getParcelableArrayList(PIAL);
	 *   </code>
	 * Interestingly, this version using an explicit instead of implicit cast fails.
	 * Reason: I believe it fails because, in Java 7, (explicit) casts apparently aren't
	 * used to determine Target Types. The explicit cast "hides" whatever the potential
	 * Target Type for T might be, so the compiler can't determine the Target Type.
	 * Per the link above (genTypeInference.html), I suspect this would work with
	 * Java 8.
	 * <p>
	 * This works:
	 *   <code>
	 *   ArrayList<ProductInfo> pial = (ArrayList<ProductInfo>)
	 *   savedInstanceState.<ProductInfo>getParcelableArrayList(PIAL);
	 *   </code>
	 * Reason: This version works, even in Java 7, because the Target Type for T is
	 * explicitly provided ("{@literal <ProductInfo>}").
	 *
	 * @param savedInstanceState Bundle passed in to onCreate(), e.g., after a
	 *                           device rotation.
	 */
	private void refreshListDisplay(Bundle savedInstanceState) {
		ProductInfoArrayList pial =
			(ProductInfoArrayList) savedInstanceState.<ProductInfo>getParcelableArrayList(PIAL);
		((ProductListRecyclerAdapter) recyclerView.getAdapter()).updateData(pial);
	}

	/**
	 * Create and initialize the RecyclerView.
	 */
	private void initRecyclerView() {
		recyclerView = (RecyclerView) findViewById(R.id.list);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(new ProductListRecyclerAdapter(this));
		recyclerView.addOnScrollListener(new ProductListOnScrollListener(this));
	}

	/**
	 * Save the array of products already pulled from the cloud so that it can be used
	 * immediately by onCreate() after a device reconfiguration.
	 *
	 * @param outState  Bundle to be passed in to onCreate(), e.g., after a
	 *                     device rotation.
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		ProductInfoArrayList pial =
			((ProductListRecyclerAdapter) recyclerView.getAdapter()).getProductInfoArrayList();

		outState.putParcelableArrayList(PIAL, pial);
	}

    /**
     * Tracing method for app overall.
     *
     * @param msg message to be logged.
     */
    static public void trace(String msg) {
        Support.trc(Configure.App.TRACE, "App", msg);
    }
}
