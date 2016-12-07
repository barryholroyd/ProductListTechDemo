package com.barryholroyd.prodlisthpdemo.product_info;

import android.app.Activity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

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

        if (GetProducts.instance.areAllItemsRead()) {
			return;
		}

        synchronized (this) {
            /*
             * Collect row information.
             */
            LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();

            // Total number of items currently in the adapter, including the header row.
            int totalLoadedRows = llm.getItemCount();

            /*
              * Get the adapter position of the last row fully displayed by RecyclerView.
              * Add:
              *   The adapter position of the first row fully displayed by RecyclerView.
              *   The total number of items being actively displayed by RecyclerView.
              * The latter includes the manually added header row.
              */
            int lastVisibleRow = llm.findFirstVisibleItemPosition() + recyclerView.getChildCount();

            /*
             * If a load is underway:
             *   1. Check to see if it has completed. If not, just return.
             *   2. Otherwise, updated the total loaded, set loading to false and continue.
             * A load is completed when the adapter has had additional rows added to it.
             * This will become apparent all at once, since it doesn't become visible until
             * the adapter's notifyDataSetChanged() has been called.
             */
            if (loading) {
                if (totalLoadedRows == totalLoadedRowsPrevious)
                    return;
                totalLoadedRowsPrevious = totalLoadedRows;
                loading = false;
            }

            /*
             * If get this far, there isn't a load in progress.
             * Pre-load data if appropriate.
             *
             * Data should be pre-loaded when the last row that the user can see,
             * plus an arbitrary look-ahead "trigger distance", is greater than
             * the highest row loaded so far.
             */
            if (lastVisibleRow + TRIGGER_DISTANCE > totalLoadedRows) {
                loading = true;
                GetProducts.instance.getProductBatch(a);
            }
        }
	}
}
