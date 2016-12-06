package com.barryholroyd.productsdemo.product_info;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.barryholroyd.productsdemo.support.Support;

/**
 * Load additional data (more product information from the cloud) in advance
 * of actually needing to display it.
 * <p>
 * Overview:
 * <br>
 * When the visible leading edge (the last row displayed at the bottom of
 * the RecyclerView display) approaches the last row currently loaded into
 * the backing array, we need to pre-fetch additional rows from the cloud
 * and load them into the backing array so that scrolling pauses are minimized.
 * <p>
 * Note: All row references are for the backing array.
 * <p>
 * Definitions:
 * <dl>
 *   <dt>firstVisibleRow
 *   <dd>first visible row
 *   <dt>totalVisibleRows
 *   <dd>total number of visible rows
 *   <dt>lastVisibleRow
 *   <dd>last visible row (firstVisibleRow + totalVisibleRows)
 *   <dt>TRIGGER_DISTANCE
 *   <dd>look-ahead distance -- triggers pre-loading from cloud
 *   <dt>totalLoadedRows
 *   <dd>total number of rows currently loaded from the cloud
 *   <dt>batchSize
 *   <dd>batch size to load from the could into the adapter (defined in GetProducts).
 * </dl>
 * <p>
 * Algorithm: When {@code lastVisibleRow + triggerDistance > lastLoadedRow},
 * load batchSize more rows from the cloud into the adapter.
 *
 * @author Barry Holroyd
 */
public class ProductListOnScrollListener extends RecyclerView.OnScrollListener
{
	/** The look-ahead distance -- triggers preloading from cloud. */
	private static final int TRIGGER_DISTANCE = 50;

	/**
     * Saved value of totalLoadedRows; used to determine when adapter array has been refreshed.
     * It starts at 1 because a header row gets added manually to the adapter's set of rows.
     */
	private int totalLoadedRowsPrevious = 1;

	/** True when the adapter is loading more data into the backing array from the cloud. */
	private boolean loading = true;

	/** Context for GetProducts method calls. */
	Activity a;

	public ProductListOnScrollListener(Activity _a) {
		super();
		a = _a;
	}

	/**
     * Standard onScrolled() callback for RecylerView.
     * <p>
	 *     Called multiple times when a user is scrolling the RecyclerView. Since this will be
	 *     called many times a second during the scrolling, we want to limit the amount of
	 *     processing here as much as possible. We also want to minimize the time we have
     *     code here locked.
     * <p>
     *     Since we are interested in the visible rows, we include the manually added
     *     header row in the row count.
	 *
	 * @param recyclerView the RecyclerView being scrolled.
	 * @param dx the horizontal distance scrolled in pixels.
	 * @param dy the vertical distance scrolled in pixels.
	 */
	@Override
	public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
		super.onScrolled(recyclerView, dx, dy);

        Support.logd(String.format("xxx areAllItemsRead(): %b",
                GetProducts.instance.areAllItemsRead())); // DEL:

        if (GetProducts.instance.areAllItemsRead()) {
			Support.logd(String.format("************* areAllItemsRead(): %b", true)); // DEL:
			return;
		}

//        synchronized (this) {
            LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();

//        }

		// Total number of items currently in the adapter plus the header row.
		int totalLoadedRows = llm.getItemCount();

		//DEL:
		Support.logd(String.format("LOADED/PREV: %d/%d", totalLoadedRows, totalLoadedRowsPrevious));

		// If the adapter somehow has *fewer* rows than previously, adjust accordingly.
		if (totalLoadedRows < totalLoadedRowsPrevious) { // DEL:
            // This should never happen. TBD:
            throw new IllegalStateException(String.format(
                    "Too few rows: totalLoadedRows=%d totalLoadedRowsPresvious=%d",
                    totalLoadedRows, totalLoadedRowsPrevious));
//			Support.logd(String.format("@@@@@ ERROR ***** FEWER ROWS (?!)")); // DEL:
//			totalLoadedRowsPrevious = totalLoadedRows;
//			// If it has no rows at all, go ahead and start reloading them.
//			loading = true;
		}

		// The adapter position of the first row fully displayed by RecyclerView.
		int firstVisibleRow = llm.findFirstVisibleItemPosition();

		// The total number of items being actively displayed by RecyclerView.
        // This already includes the manually added header row.
		int totalVisibleRows = recyclerView.getChildCount();
		
		// The adapter position of the last row fully displayed by RecyclerView.
		int lastVisibleRow = firstVisibleRow + totalVisibleRows;

		// DEL:
		Support.logd(String.format(
				"LOADED/PREV: %d/%d (loading=%b), vrFirst=%d  + vrTotal=%d => vrLast=%d",
				totalLoadedRows, totalLoadedRowsPrevious, loading,
				firstVisibleRow, totalVisibleRows, lastVisibleRow
		));

		/**
		 * If a load is underway, check to see if it has completed and update the loading
		 * flag appropriately. A load has completed when the adapter has added additional rows
		 * to its backing array. This will become apparent only after the adapter's
		 * notifyDataSetChanged() has been called.
		 */
		if (loading && (totalLoadedRows > totalLoadedRowsPrevious)) {
			Support.logd(String.format("@@@@@ Loading completed...")); // DEL:
			totalLoadedRowsPrevious = totalLoadedRows;
			loading = false;
		}

		/*
		 * If we aren't already loading and we need to pre-load more data, get
		 * the next batch of rows.
		 */
		if (!loading && (lastVisibleRow + TRIGGER_DISTANCE > totalLoadedRows)) {
			Support.logd(String.format("@@@@@ GET NEXT BATCH OF ROWS")); // DEL:
			loading = true;
			GetProducts.instance.getProductBatch(a);
		}
	}
}
