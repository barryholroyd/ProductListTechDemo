The json directory contains the full list of Walmart Products
available via this demo. As long as the API_KEY is valid, it can be
recreated using bin/runme.

It seemed like a good idea to copy all of the product information so that the
app (with slight modifications) could continue to work even if the API_Key
stops working.

Notes:
  o The maximum page size is 30.
  o There are currently 224 products available.
  o The eight json file (7.json) contains 30 products instead of only 3.
    This is probably due to a bug in the server. See bin/runme for
    more information.

API:
  Demo link:	API_PREFIX + API_KEY + PAGE_NUMBER + PAGE_SIZE
  Example:	https://walmartlabs-test.appspot.com/_ah/api/walmart
  		v1/walmartproducts/cec8e676-d56f-49b9-987a-989a9d23a724/0/25
