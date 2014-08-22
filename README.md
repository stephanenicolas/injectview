injectview
==========

***Injects all views inflated from XML automatically.***

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