package com.barryholroyd.productlisttechdemo;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.barryholroyd.productlisttechdemo.config.SettingsManager;
import com.barryholroyd.productlisttechdemo.product_info.GetProducts;
import com.barryholroyd.productlisttechdemo.product_info.ProductInfo;
import com.barryholroyd.productlisttechdemo.product_info.ProductInfoArrayList;
import com.barryholroyd.productlisttechdemo.product_info.ProductListOnScrollListener;
import com.barryholroyd.productlisttechdemo.product_info.ProductListRecyclerAdapter;
import com.barryholroyd.productlisttechdemo.support.ActivityPrintStates;
import com.barryholroyd.productlisttechdemo.config.Settings;
import com.barryholroyd.productlisttechdemo.support.Support;

/**
 * Display a list of products (this is the main Activity).
 * <p>
 * Clicking on a product will call ActivityProductInfo to display product-specific information.
 *
 * @author Barry Holroyd
 * @see    <a href="https://developer.walmartlabs.com/">Walmart Open API</a>
 */
public class ActivityProductList extends ActivityPrintStates
{
	/**
	 * Logger tag for this application.
	 */
	static final public String LOGTAG = "ProductListTechDemo";
	/**
	 * RecyclerView used to display the list of products pulled from the cloud.
	 */
	private RecyclerView recyclerView;

	/**
	 * Key for storing an instance of the ProductInfoArrayList object. Get
	 * it with getParcelableArrayList().
	 */
	static final private String PIAL = "PIAL";

	/** Getter for recyclerView. */
	final public RecyclerView getRecyclerView() { return recyclerView; }

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

		// Settings/preferences.
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        Settings.init(this);
        SettingsManager.init(this);

		// Appbar
		Toolbar myToolbar =(Toolbar) findViewById(R.id.appbar);
		setSupportActionBar(myToolbar);

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
	 * This fails:
         * <pre>
	 *   ProductInfoArrayList pial = ((ArrayList<ProductInfo>) savedInstanceState.getParcelableArrayList(PIAL));
         * </pre>
	 * Reason: {@code ArrayList<ProductInfo>} is not a subclass of {@code ArrayList<Parcelable>}, or
	 * vice-versa, even though {@code ProductInfo} is a subclass of {@code Parcelable}.
	 * <p>
         * This works:
         * <pre>
         *   ArrayList<ProductInfo> pial = savedInstanceState.getParcelableArrayList(PIAL);
         *   ProductInfoArrayList pial2 = (ProductInfoArrayList) pial;
         * </pre>
	 * Reason: {@code getParcelableArrayList()} returns an {@code ArrayList<T extends Parcelable>}.
	 * Since {@code ProductInfo} extends {@code Parcelable}, it is a valid Target Type for {@code T}.
	 * Since {@code ProductInfoArrayList} extends {@code ArrayList<ProductInfo>}, {@code pial} can
	 * be assigned to {@code pail2}. However, since this is a downcast ({@code pial} could actually
	 * be some other subclass of {@code ArrayList<ProductInfo>}), an explicit cast has to be used.
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
    public static void trace(String msg) {
        Support.trc(Settings.isAppTrace(), "App", msg);
    }
}
