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

### Related projects 

On the same principle of byte code weaving : 

* [InjectExtra](https://github.com/stephanenicolas/injectextra)
* [InjectResource](https://github.com/stephanenicolas/injectresource)
* [LogLifeCycle](https://github.com/stephanenicolas/loglifecycle)
* [Hugo](https://github.com/jakewharton/hugo)

### CI 

[![Travis Build](https://travis-ci.org/stephanenicolas/injectview.svg?branch=master)](https://travis-ci.org/stephanenicolas/injectview)
[![Coverage Status](https://img.shields.io/coveralls/stephanenicolas/injectview.svg)](https://coveralls.io/r/stephanenicolas/injectview)

License
-------

	Copyright (C) 2014 Stéphane NICOLAS

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	     http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
