The SampleProductSet.json file contains the same text that is returned by a
single call to the Walmart demo link. All of the products are pulled down
(2000 were requested; only 224 exist), so a single call suffices. When
working with the real size, smaller batches can be pulled down (e.g., 10
products at a time). Nonetheless, it seemed like a good idea to copy all of
the product information so that the app (with slight modifications) could
continue to work even if the API_Key stops working.

Notes:
  o The maximum page size is 30.
  o There are currently 224 products available.

API:

  Demo link:	API_PREFIX + API_KEY + PAGE_NUMBER + PAGE_SIZE
  Example:	https://walmartlabs-test.appspot.com/_ah/api/walmart
  		v1/walmartproducts/cec8e676-d56f-49b9-987a-989a9d23a724/0/25

Use the following to get samples:

  FIRST BATCH:
    wget 'https://walmartlabs-test.appspot.com/_ah/api/walmart/v1/
         walmartproducts/cec8e676-d56f-49b9-987a-989a9d23a724/0/25'

  LAST BATCH:
    wget 'https://walmartlabs-test.appspot.com/_ah/api/walmart/v1/
         walmartproducts/cec8e676-d56f-49b9-987a-989a9d23a724/8/25'
