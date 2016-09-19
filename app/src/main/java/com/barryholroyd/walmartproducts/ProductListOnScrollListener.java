package com.barryholroyd.walmartproducts;

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
 *   <dd>look-ahead distance -- triggers preloading from cloud
 *   <dt>totalLoadedRows
 *   <dd>total number of rows currently loaded from the cloud
 *   <dt>batchSize
 *   <dd>batch size to load from the could into the adapter (defined in GetProducts).
 * </dl>
 * <p>
 * Algorithm: When {@code lastVisibleRow+triggerDistance > lastLoadedRow},
 * load batchSize more rows from the cloud into the adapter.
 *
 * @author Barry Holroyd
 */
public class ProductListOnScrollListener extends RecyclerView.OnScrollListener
{
	/** The look-ahead distance -- triggers preloading from cloud. */
	private static final int TRIGGER_DISTANCE = 25;

	/** The adapter position of the first row fully displayed by RecyclerView. */
	private int firstVisibleRow;

	/** The total number of items being actively displayed by RecyclerView. */
	private int totalVisibleRows;

	/** The adapter position of the last row fully displayed by RecyclerView. */
	private int lastVisibleRow;

	/** Total number of items currently in the adapter. */
	private int totalLoadedRows;

	/** Saved value of totalLoadedRows; used to determine when adapter array has been refreshed. */
	private int totalLoadedRowsPrevious = 0;

	/** True when the adapter is loading more data into the backing array from the cloud. */
	private boolean loading = true;

	// This happens many times a second during a scroll, so we want to limit the amount
	// of processing here as much as possible.
	/**
	 * Called multiple times when a user is scrolling the RecyclerView. Since this will be
	 * called many times a second during the scrolling, we want to limit the amount of
	 * processing here as much as possible.
	 *
	 * @param recyclerView the RecyclerView being scrolled.
	 * @param dx the horizontal distance scrolled in pixels.
	 * @param dy the vertical distance scrolled in pixels.
	 */
	@Override
	public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
		super.onScrolled(recyclerView, dx, dy);

		LinearLayoutManager llm = (LinearLayoutManager) recyclerView.getLayoutManager();
		totalLoadedRows = llm.getItemCount(); // number of rows in the adapter
		if (totalLoadedRows == GetProducts.instance.getMaxProducts()) {
			Support.logd("All data has been loaded already.");
			return;
		}
		if (totalLoadedRows > GetProducts.instance.getMaxProducts()) {
			throw new IllegalStateException("Unhandled: reduction in number of products available");
		}

		// If the adapter somehow has *fewer* rows than previously, adjust accordingly.
		if (totalLoadedRows < totalLoadedRowsPrevious) {
			Support.logd("Adjusting for reduction in the number of rows already loaded...");
			totalLoadedRowsPrevious = totalLoadedRows;
			// If it has no rows at all, go ahead and start reloading them.
			loading = true;
		}

		firstVisibleRow = llm.findFirstVisibleItemPosition(); // first row actively displayed
		totalVisibleRows = recyclerView.getChildCount();      // number of rows actively displayed
		lastVisibleRow = firstVisibleRow + totalVisibleRows;  // last row actively displayed

		Support.logd(String.format("firstVisibleRow:         %d\n", firstVisibleRow));
		Support.logd(String.format("totalVisibleRows:        %d\n", totalVisibleRows));
		Support.logd(String.format("lastVisibleRow:          %d\n", lastVisibleRow));
		Support.logd(String.format("totalLoadedRows:         %d\n", totalLoadedRows));
		Support.logd(String.format("totalLoadedRowsPrevious: %d\n", totalLoadedRowsPrevious));
		Support.logd(String.format("loading:                 %b\n", loading));
		Support.logd("-------------------------------------\n");

		/**
		 * If a load is underway, check to see if it has completed and update the loading
		 * flag appropriately. A load has completed when the adapter has added additional rows
		 * to its backing array. This will become apparent only after the adapter's
		 * notifyDataSetChanged() has been called.
		 */
		if (loading && (totalLoadedRows > totalLoadedRowsPrevious)) {
			Support.logd("Loading completed.");
			totalLoadedRowsPrevious = totalLoadedRows;
			loading = false;
		}

		/*
		 * If we aren't already loading and we need to pre-load more data, get
		 * the next batch of rows.
		 */
		if (!loading && (lastVisibleRow + TRIGGER_DISTANCE > totalLoadedRows)) {
			Support.logd("Loading more data...");
			loading = true;
			GetProducts.instance.getNextBatch();
		}
	}
}
