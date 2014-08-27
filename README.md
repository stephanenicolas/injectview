InjectView [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.stephanenicolas.injectview/injectview-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.stephanenicolas.injectview/injectview-plugin)
==========

***On Android, Injects all views inflated from XML automatically. No boiler plate at all.***

<img src="https://raw.github.com/stephanenicolas/injectview/master/assets/injectview-logo.jpg"
width="250px" />

###Usage

TODO (below is not relevant yet)

Inside your `build.gradle` file, add : 

```groovy
apply plugin: 'injectview'
```

And now, annotate every `Activity`, `Fragment` or whatever you want to see get views injected. 

```java

public class MainActivity extends Activity {
   @InjectView private View view;
}
```

###Example

You will find an example program in the repo.

###How does it work ?

Thanks to 
* [morpheus](https://github.com/stephanenicolas/morpheus), byte code weaver for android.
* [AfterBurner](https://github.com/stephanenicolas/afterburner), byte code weaving swiss army knife for Android.

###TODO

* [ ] add CI and badge
* [ ] support findbugs nullable
 
### Related projects 

On the same principle of byte code weaving : 

* [InjectExtra](https://github.com/stephanenicolas/injectextra)
* [InjectResource](https://github.com/stephanenicolas/injectresource)
* [LogLifeCycle](https://github.com/stephanenicolas/loglifecycle)
* [Hugo](https://github.com/jakewharton/hugo)

