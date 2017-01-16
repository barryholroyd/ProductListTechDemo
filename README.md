# Product List Technical Demo
ProductListTechnicalDemo is technical demonstration of a high performance "list" implementation.

* Images downloaded in the background. Two implementations are provided: AsyncTask-based and threads-based.
* Initial image url retained as a field and compared to the (potentially updated) URL of the ViewHolder upon image arrival.
* Configurable look-ahead pre-loading.
* Configurable memory cache.
* Configurable disk cache.

There are numerous cache configuration settings available so that you can see the effects of the caches in use.
As this is a technical demo its user-facing functionality is small (a simple list of products pulled from Walmart via the Walmart Open API along with a separate screen to display individual products), relatively little attention was paid to the user interface

Full [javadoc](http://barryholroyd.github.io/android/apps/ProductListTechDemo/javadoc/) documentation is available.
