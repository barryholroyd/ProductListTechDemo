# ProdlistHpDemo
ProdListHpDemo is a well-documented, well-written technical demonstration of a high performance "list" implementation.

I recommend this code base to anyone who would like to get a feel for the quality of code that I write in a professional capacity. In addition to being well designed and well documented, it provides the following performance-related features.
* Images downloaded in the background. Two implementations are provided: AsyncTask-based and threads-based.
* Initial image url retained as a field and compared to the (potentially updated) URL of the ViewHolder upon image arrival.
* Configurable look-ahead pre-loading.
* Configurable memory cache.
* Configurable disk cache.
There are numerous cache configuration settings available so that you can see the effects of the caches in use.
As this is a technical demo its user-facing functionality is small (a simple list of products pulled from Walmart via the Walmart Open API along with a separate screen to display individual products). Relatively little attention was paid to the user interface

Full [javadoc](http://barryholroyd.github.io/android/apps/prodlisthpdemo/javadoc/) documentation is available.
