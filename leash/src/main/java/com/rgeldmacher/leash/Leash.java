/*
 * Copyright 2015 Robert Geldmacher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.rgeldmacher.leash;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Keep hold of your objects during configuration changes. Use this class to
 * retain and restore fields annotated with {@literal @}{@link Retain}.
 * <p/>
 * Call {@linkplain #restore(Activity)} in your Activity or
 * {@linkplain #restore(Fragment)} in your Fragment to restore objects you have retained before
 * a configuration change.
 * <br/>
 * Call {@linkplain #retain(Activity)} in your Activity or
 * {@linkplain #retain(Fragment)} in your Fragment to retain objects across a
 * a configuration change:
 * <p/>
 * <pre><code>
 * public class ExampleActivity extends Activity {
 *
 *    {@literal @}Retain
 *     List<Foo> data;
 *
 *     protected void onCreate(Bundle savedInstanceState) {
 *         super.onCreate(savedInstanceState);
 *         Leash.restore(this);
 *         // use your retained data ...
 *     }
 *
 *     protected void onSaveInstanceState(Bundle outState) {
 *         super.onSaveInstanceState(outState);
 *         Leash.retain(this);
 *     }
 * </code></pre>
 *
 *
 * @author rgeldmacher
 */
public final class Leash {

    /**
     * Suffix that is added to the Leash classes.
     */
    static final String SUFFIX = "Leash";

    private Leash() {
        // prevent instantiation
    }

    /**
     * Restore fields that have been previously retained. The annotated fields in the
     * Activity will be set to the objects and values that were assigned to them on the
     * last call to {@linkplain #retain(Activity)}.
     * <br>
     * If {@linkplain #retain(Activity)} has not been called before, e.g. on the first
     * Activity start, the annotated fields will not be changed.
     *
     * @param source the Activity containing the annotated fields
     */
    public static void restore(Activity source) {
        callLeash("restore", source);
    }

    /**
     * Restore fields that have been previously retained. The annotated fields in the
     * Fragment will be set to the objects and values that were assigned to them on the
     * last call to {@linkplain #retain(Fragment)}.
     * <br>
     * If {@linkplain #retain(Fragment)} has not been called before, e.g. on the first
     * Fragment start, the annotated fields will not be changed.
     *
     * @param source the Fragment containing the annotated fields
     */
    public static void restore(Fragment source) {
        callLeash("restore", source);
    }

    /**
     * Restore fields that have been previously retained. The annotated fields in the
     * Activity will be set to the objects and values that were assigned to them on the
     * last call to {@linkplain #retain(FragmentActivity)}.
     * <br>
     * If {@linkplain #retain(FragmentActivity)} has not been called before, e.g. on the first
     * Activity start, the annotated fields will not be changed.
     *
     * @param source the Activity containing the annotated fields
     */
    public static void restore(FragmentActivity source) {
        callLeash("restore", source);
    }

    /**
     * Restore fields that have been previously retained. The annotated fields in the
     * Fragment will be set to the objects and values that were assigned to them on the
     * last call to {@linkplain #retain(android.support.v4.app.Fragment)}.
     * <br>
     * If {@linkplain #retain(android.support.v4.app.Fragment)} has not been called before, e.g. on the first
     * Fragment start, the annotated fields will not be changed.
     *
     * @param source the Fragment containing the annotated fields
     */
    public static void restore(android.support.v4.app.Fragment source) {
        callLeash("restore", source);
    }

    /**
     * Retain the objects and values currently set on the annotated fields in the Activity.
     * These values can be restored after the configuration change by calling {@linkplain #restore(Activity)}.
     * <p/>
     * A good place to call this method is in {@linkplain Activity#onSaveInstanceState(Bundle)}.
     * <p/>
     * Note: This method must not be called after {@linkplain Activity#onSaveInstanceState(Bundle)}
     * with regards to the Activity's life cycle, in particular don't call this method in
     * {@linkplain Activity#onStop()} or {@linkplain Activity#onDestroy()}.
     *
     * @param source the Activity containing the annotated fields
     */
    public static void retain(Activity source) {
        callLeash("retain", source);
    }

    /**
     * Retain the objects and values currently set on the annotated fields in the Fragment.
     * These values can be restored after the configuration change by calling {@linkplain #restore(Fragment)}.
     * <p/>
     * A good place to call this method is in {@linkplain Fragment#onSaveInstanceState(Bundle)}.
     * <p/>
     * Note: This method must not be called after {@linkplain Fragment#onSaveInstanceState(Bundle)}
     * with regards to the Fragment's life cycle, in particular don't call this method in
     * {@linkplain Fragment#onStop()} or {@linkplain Fragment#onDestroy()}.
     *
     * @param source the Fragment containing the annotated fields
     */
    public static void retain(Fragment source) {
        callLeash("retain", source);
    }

    /**
     * Retain the objects and values currently set on the annotated fields in the Activity.
     * These values can be restored after the configuration change by calling {@linkplain #restore(FragmentActivity)}.
     * <p/>
     * A good place to call this method is in {@linkplain Activity#onSaveInstanceState(Bundle)}.
     * <p/>
     * Note: This method must not be called after {@linkplain Activity#onSaveInstanceState(Bundle)}
     * with regards to the Activity's life cycle, in particular don't call this method in
     * {@linkplain Activity#onStop()} or {@linkplain Activity#onDestroy()}.
     *
     * @param source the Activity containing the annotated fields
     */
    public static void retain(FragmentActivity source) {
        callLeash("retain", source);
    }

    /**
     * Retain the objects and values currently set on the annotated fields in the Fragment.
     * These values can be restored after the configuration change by calling {@linkplain #restore(android.support.v4.app.Fragment)}.
     * <p/>
     * A good place to call this method is in {@linkplain android.support.v4.app.Fragment#onSaveInstanceState(Bundle)}.
     * <p/>
     * Note: This method must not be called after {@linkplain android.support.v4.app.Fragment#onSaveInstanceState(Bundle)}
     * with regards to the Fragment's life cycle, in particular don't call this method in
     * {@linkplain android.support.v4.app.Fragment#onStop()} or {@linkplain android.support.v4.app.Fragment#onDestroy()}.
     *
     * @param source the Fragment containing the annotated fields
     */
    public static void retain(android.support.v4.app.Fragment source) {
        callLeash("retain", source);
    }

    /**
     * Removes the references to the retained objects in the Leash holder.
     * This may be useful if you want to release objects when navigating from the current activity.
     * <p/>
     * Retained primitive types will be set to their default values.
     *
     * @param source the Activity containing the annotated fields
     */
    public static void clear(Activity source) {
        callLeash("clear", source);
    }

    /**
     * Removes the references to the retained objects in the Leash holder.
     * This may be useful if you want to release objects when navigating from the current activity.
     * <p/>
     * Retained primitive types will be set to their default values.
     *
     * @param source the Activity containing the annotated fields
     */
    public static void clear(FragmentActivity source) {
        callLeash("clear", source);
    }

    /**
     * Removes the references to the retained objects in the Leash holder.
     * This may be useful if you want to release objects when navigating from the current fragment.
     * <p/>
     * Retained primitive types will be set to their default values.
     *
     * @param source the Fragment containing the annotated fields
     */
    public static void clear(Fragment source) {
        callLeash("clear", source);
    }

    /**
     * Removes the references to the retained objects in the Leash holder.
     * This may be useful if you want to release objects when navigating from the current fragment.
     * <p/>
     * Retained primitive types will be set to their default values.
     *
     * @param source the Fragment containing the annotated fields
     */
    public static void clear(android.support.v4.app.Fragment source) {
        callLeash("clear", source);
    }

    private static void callLeash(String methodName, Object source) {
        try {
            String leashClassName = source.getClass().getName() + SUFFIX;
            Method method = Class.forName(leashClassName).getMethod(methodName, source.getClass());
            method.invoke(null, source);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to access Leash for " + source.getClass().getName(), e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Failed to access Leash for " + source.getClass().getName(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access Leash for " + source.getClass().getName(), e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to access Leash for " + source.getClass().getName(), e);
        }
    }
}
