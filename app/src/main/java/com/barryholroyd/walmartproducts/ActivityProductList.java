package com.barryholroyd.walmartproducts;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.ArrayList;

/*
 * BUG: Buttons initially appear and then disappear on startup.
 * BUG: Header fields should be centered horizontally.
 * BUG: Scrolling: causes spaces between rolls.
 * BUG: HTML in description.
 * BUG: Rotating device adds more rows. Instead, should remember where you are. Same for going list->product->list.
 *      Solution: see http://stackoverflow.com/questions/37238293/display-stored-data-after-rotation-in-a-recyclerview.
 *      Make ProductInfo parcelable and recreate the array.
 * BUG: Scrolling down, rotating the device, then scrolling up may require reloading JSON data.
 * BUG: Be sure that the correct image is displayed despite loading from URL set in background thread.
 * BUG: Finish product display page.
 */

/**
 * List the Walmart products.
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
	 *
	 * @param savedInstanceState used to transfer the backing array for the products
	 *                           across device rotations.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		new Support(this);
		setContentView(R.layout.productlist);
		init();
		if (savedInstanceState == null) {
			GetProducts.instance.getNextBatch();
		}
		else {
			// Display with existing data.

			/*
			 * This works:
			 *   ArrayList<ProductInfo> pial = savedInstanceState.getParcelableArrayList(PIAL);
			 *   ProductInfoArrayList pial2 = (ProductInfoArrayList) pial;
			 * Reason: getParcelableArrayList() returns an ArrayList<T extends Parcelable>.
			 * Since ProductInfo extends Parcelable, it is a valid Target Type for T.
			 * Since ProductInfoArrayList extends ArrayList<ProductInfo>, pial can
			 * be assigned to pail2. However, since this is a downcast (pial could actually
			 * be some other subclass of ArrayList<ProductInfo>), an explicit cast has to
			 * be used.
			 *
			 * This fails:
			 *   ProductInfoArrayList pial = ((ArrayList<ProductInfo>)
			 *     savedInstanceState.getParcelableArrayList(PIAL));
			 * Reason: ArrayList<ProductInfo> is not a subclass of ArrayList<Parcelable>, or
			 * vice-versa, even though ProductInfo is a subclass of Parcelable.
			 *
			 * For the following, see:
			 *   http://docs.oracle.com/javase/tutorial/java/generics/genTypeInference.html,
			 *   the discussion of processStringList().
			 *
			 * This fails:
			 *   ArrayList<ProductInfo> pialBad =
			 *     (ArrayList<ProductInfo>) savedInstanceState.getParcelableArrayList(PIAL);
			 * Interestingly, this version using an explicit instead of implicit cast fails.
			 * Reason: I believe it fails because, in Java 7, (explicit) casts apparently aren't
			 * used to determine Target Types. The explicit cast "hides" whatever the potential
			 * Target Type for T might be, so the compiler can't determine the Target Type.
			 * Per the link above (genTypeInference.html), I suspect this would work with
			 * Java 8.
			 *
			 * This works:
			 *   ArrayList<ProductInfo> pial = (ArrayList<ProductInfo>)
			 *   savedInstanceState.<ProductInfo>getParcelableArrayList(PIAL);
			 * Reason: This version works, even in Java 7, because the Target Type for T is
			 * explicitly provided ("<ProductInfo>").
			 */

			// TBD: How to know which rows to display?
			ProductInfoArrayList pial =
				(ProductInfoArrayList) savedInstanceState.<ProductInfo>getParcelableArrayList(PIAL);
			((ProductListRecyclerAdapter) recyclerView.getAdapter()).updateData(pial);
		}
	}

	private void init() {
		recyclerView = (RecyclerView) findViewById(R.id.list);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(new ProductListRecyclerAdapter());
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		ProductInfoArrayList pial =
			((ProductListRecyclerAdapter) recyclerView.getAdapter()).getProductInfoArrayList();
		outState.putParcelableArrayList(PIAL, pial);
	}

	/**
	 * Temporary callback for button clicks in the Product List.
	 * TBD: delete when no longer needed.
	 *
	 * @param view The button clicked on.
	 */
	public void buttonTmp(View view) {
		switch (view.getId()) {
			case R.id.button_productinfo:
				startActivity(new Intent(this, ActivityProductInfo.class));
				break;
			case R.id.button_productlist:
				GetProducts.instance.getNextBatch();
				break;
		}
	}
}
