package com.barryholroyd.productsdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/*
 * TODO: ProductInfo page: move the image into the Price row.
 * TODO: Product Info page: use PiTextStyle for all TextViews.
 * TODO: Code unit tests.
 * TODO: GUI unit tests.
 * TBD: no image for header
 * BUG: Back button (push into background onto Overview screen), bring foreground: crashes with:
 * Total downloaded count corrupted (totalDownloaded=75 itemCount=25.
 * TBD: ALL PRODUCTS button doesn't show if there is a long description
 * (product info page, landscape mode). E.g.: Tivo Romio Pro.
 * TBD: Add blank icon to header row.
 * TBD: Display correct image on individual product page.
 *
 * "NTH" means "Nice to have"
 * NTH: Product Info screen's table should have the same border coloring as the Product List screen.
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
public class ActivityProductList extends AppCompatActivity
{
	/**
	 * Logger tag for this application.
	 */
	static final public String LOGTAG = "WalmartProduct";
	/**
	 * RecyclerView used to display the list of products pulled from the cloud.
	 */
	static RecyclerView recyclerView;

	/**
	 * Key for storing an instance of the ProductInfoArrayList object. Get
	 * it with getParcelableArrayList().
	 */
	static final String PIAL = "PIAL";

	/**
	 * Sandard onCreate method.
	 *
	 * @param savedInstanceState used to transfer the backing array for the products
	 *                           across device rotations.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.productlist);

		initRecyclerView();
		if (savedInstanceState == null) {
			// Display the product list for the first time.
			GetProducts.instance.getNextPage(this);
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
}
