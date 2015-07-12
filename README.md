# Leash
Annotation support for retaining objects during orientation change on Android.

## Introduction
Annotate fields in your Activity or Fragment to retain them when a configuration change occurs.

```
public class ExampleActivity extends Activity {

    @Retain
    List<Foo> data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ExampleActivityLeash.getRetainedData(this);
        // use your retained data ...
    }
  
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ExampleActivityLeash.retainData(this);
    }
```

The annotation will generate code at compile time and create the class `<YourActivity>Leash` that provides the methods `getRetainedData()` and `retainData()`. Call `getRetainedData()` to initialize the annotated fields of your activity with the retained objects. Call `retainData()` in `onDestroy()` to store the objects you want to retain.

## How it works
The basic idea is to store objects in a fragment that is retained across configuration changes, as described by Google in the [API Guides](http://developer.android.com/guide/topics/resources/runtime-changes.html#RetainingAnObject).
The generated code will create a retained fragment with fields matching the annotated fields of your Activity. When `retainData()`is called the objects of the annotated fields will be stored in the retained fragment. When `getRetainedData()` is called after the configuration change the objects of the retained fragment will be assigned to the annotated fields of your activity again.

## Download

## Developed by
Robert Geldmacher - [google.com/+RobertGeldmacher](https://plus.google.com/+RobertGeldmacher)

## Credits
[Hugo Visser](https://plus.google.com/+HugoVisser) - Author of the invaluable [android-apt plugin](https://bitbucket.org/hvisser/android-apt) and [bundles](https://bitbucket.org/hvisser/bundles) which was a great inspiration for leash.

## License
Copyright [2015] [Robert Geldmacher]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.