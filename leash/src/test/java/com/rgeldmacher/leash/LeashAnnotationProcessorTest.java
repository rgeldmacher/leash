/*
 * Copyright 2015 Robert Geldmacher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rgeldmacher.leash;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.ASSERT;
import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;

/**
 * @author rgeldmacher
 */
public class LeashAnnotationProcessorTest {

    @Test
    public void testProcessActivity() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
                "package test;",
                "import android.app.Activity;",
                "import com.rgeldmacher.leash.Retain;",
                "public class Test extends Activity {",
                "    @Retain",
                "    Object thing;",
                "}"
        ));

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestLeash",
                Joiner.on('\n').join(
                        "package test;",
                        "import android.app.Activity;",
                        "import android.app.Fragment;",
                        "import android.app.FragmentManager;",
                        "import java.lang.Object;",
                        "",
                        "public final class TestLeash {",
                        "  private TestLeash() {",
                        "  }",
                        "",
                        "  public static void restore(Test activity) {",
                        "    TestRetainedDataFragment retainedFragment = getRetainedFragment(activity);",
                        "    if (retainedFragment != null) {",
                        "      if (retainedFragment.hasBeenRetained) {",
                        "        activity.thing = retainedFragment.thing;",
                        "      }",
                        "    }",
                        "  }",
                        "",
                        "  public static void retain(Test activity) {",
                        "    TestRetainedDataFragment retainedFragment = getRetainedFragment(activity);",
                        "    if (retainedFragment != null) {",
                        "      retainedFragment.thing = activity.thing;",
                        "      retainedFragment.hasBeenRetained = true;",
                        "    }",
                        "  }",
                        "",
                        "  public static void clear(Test activity) {",
                        "    TestRetainedDataFragment retainedFragment = getRetainedFragment(activity);",
                        "    if (retainedFragment != null) {",
                        "      retainedFragment.hasBeenRetained = false;",
                        "      retainedFragment.thing = null;",
                        "    }",
                        "  }",
                        "",
                        "  private static TestRetainedDataFragment getRetainedFragment(Activity activity) {",
                        "    if (activity != null) {",
                        "      FragmentManager fm = activity.getFragmentManager();",
                        "      if (fm != null) {",
                        "        Fragment retainedFragment = fm.findFragmentByTag(\"TestRetainedDataFragment\");",
                        "        if (retainedFragment == null) {",
                        "          retainedFragment = new TestRetainedDataFragment();",
                        "          fm.beginTransaction().add(retainedFragment, \"TestRetainedDataFragment\").commit();",
                        "        }",
                        "        if (retainedFragment instanceof TestRetainedDataFragment) {",
                        "          return (TestRetainedDataFragment) retainedFragment;",
                        "        }",
                        "      }",
                        "    }",
                        "    return null;",
                        "  }",
                        "",
                        "  public static class TestRetainedDataFragment extends Fragment {",
                        "    Object thing;",
                        "",
                        "    boolean hasBeenRetained;",
                        "",
                        "    public TestRetainedDataFragment() {",
                        "      setRetainInstance(true);}",
                        "  }",
                        "}"
                ));

        ASSERT.about(javaSource()).that(source)
                .processedWith(new LeashAnnotationProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSource);
    }

    @Test
    public void testProcessFragmentActivity() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
                "package test;",
                "import android.support.v4.app.FragmentActivity;",
                "import com.rgeldmacher.leash.Retain;",
                "public class Test extends FragmentActivity {",
                "    @Retain",
                "    Object thing;",
                "}"
        ));

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestLeash",
                Joiner.on('\n').join(
                        "package test;",
                        "import android.support.v4.app.Fragment;",
                        "import android.support.v4.app.FragmentActivity;",
                        "import android.support.v4.app.FragmentManager;",
                        "import java.lang.Object;",
                        "",
                        "public final class TestLeash {",
                        "  private TestLeash() {",
                        "  }",
                        "",
                        "  public static void restore(Test activity) {",
                        "    TestRetainedDataFragment retainedFragment = getRetainedFragment(activity);",
                        "    if (retainedFragment != null) {",
                        "      if (retainedFragment.hasBeenRetained) {",
                        "        activity.thing = retainedFragment.thing;",
                        "      }",
                        "    }",
                        "  }",
                        "",
                        "  public static void retain(Test activity) {",
                        "    TestRetainedDataFragment retainedFragment = getRetainedFragment(activity);",
                        "    if (retainedFragment != null) {",
                        "      retainedFragment.thing = activity.thing;",
                        "      retainedFragment.hasBeenRetained = true;",
                        "    }",
                        "  }",
                        "",
                        "  public static void clear(Test activity) {",
                        "    TestRetainedDataFragment retainedFragment = getRetainedFragment(activity);",
                        "    if (retainedFragment != null) {",
                        "      retainedFragment.hasBeenRetained = false;",
                        "      retainedFragment.thing = null;",
                        "    }",
                        "  }",
                        "",
                        "  private static TestRetainedDataFragment getRetainedFragment(FragmentActivity activity) {",
                        "    if (activity != null) {",
                        "      FragmentManager fm = activity.getSupportFragmentManager();",
                        "      if (fm != null) {",
                        "        Fragment retainedFragment = fm.findFragmentByTag(\"TestRetainedDataFragment\");",
                        "        if (retainedFragment == null) {",
                        "          retainedFragment = new TestRetainedDataFragment();",
                        "          fm.beginTransaction().add(retainedFragment, \"TestRetainedDataFragment\").commit();",
                        "        }",
                        "        if (retainedFragment instanceof TestRetainedDataFragment) {",
                        "          return (TestRetainedDataFragment) retainedFragment;",
                        "        }",
                        "      }",
                        "    }",
                        "    return null;",
                        "  }",
                        "",
                        "  public static class TestRetainedDataFragment extends Fragment {",
                        "    Object thing;",
                        "",
                        "    boolean hasBeenRetained;",
                        "",
                        "    public TestRetainedDataFragment() {",
                        "      setRetainInstance(true);}",
                        "  }",
                        "}"
                ));

        ASSERT.about(javaSource()).that(source)
                .processedWith(new LeashAnnotationProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSource);
    }

    @Test
    public void testProcessFragment() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
                "package test;",
                "import android.app.Fragment;",
                "import com.rgeldmacher.leash.Retain;",
                "public class Test extends Fragment {",
                "    @Retain",
                "    Object thing;",
                "}"
        ));

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestLeash",
                Joiner.on('\n').join(
                        "package test;",
                        "import android.app.Activity;",
                        "import android.app.Fragment;",
                        "import android.app.FragmentManager;",
                        "import java.lang.Object;",
                        "",
                        "public final class TestLeash {",
                        "  private TestLeash() {",
                        "  }",
                        "",
                        "  public static void restore(Test fragment) {",
                        "    Activity activity = null;",
                        "    if (fragment != null) {",
                        "      activity = fragment.getActivity();",
                        "    }",
                        "    TestRetainedDataFragment retainedFragment = getRetainedFragment(activity);",
                        "    if (retainedFragment != null) {",
                        "      if (retainedFragment.hasBeenRetained) {",
                        "        fragment.thing = retainedFragment.thing;",
                        "      }",
                        "    }",
                        "  }",
                        "",
                        "  public static void retain(Test fragment) {",
                        "    Activity activity = null;",
                        "    if (fragment != null) {",
                        "      activity = fragment.getActivity();",
                        "    }",
                        "    TestRetainedDataFragment retainedFragment = getRetainedFragment(activity);",
                        "    if (retainedFragment != null) {",
                        "      retainedFragment.thing = fragment.thing;",
                        "      retainedFragment.hasBeenRetained = true;",
                        "    }",
                        "  }",
                        "",
                        "  public static void clear(Test fragment) {",
                        "    Activity activity = null;",
                        "    if (fragment != null) {",
                        "      activity = fragment.getActivity();",
                        "    }",
                        "    TestRetainedDataFragment retainedFragment = getRetainedFragment(activity);",
                        "    if (retainedFragment != null) {",
                        "      retainedFragment.hasBeenRetained = false;",
                        "      retainedFragment.thing = null;",
                        "    }",
                        "  }",
                        "",
                        "  private static TestRetainedDataFragment getRetainedFragment(Activity activity) {",
                        "    if (activity != null) {",
                        "      FragmentManager fm = activity.getFragmentManager();",
                        "      if (fm != null) {",
                        "        Fragment retainedFragment = fm.findFragmentByTag(\"TestRetainedDataFragment\");",
                        "        if (retainedFragment == null) {",
                        "          retainedFragment = new TestRetainedDataFragment();",
                        "          fm.beginTransaction().add(retainedFragment, \"TestRetainedDataFragment\").commit();",
                        "        }",
                        "        if (retainedFragment instanceof TestRetainedDataFragment) {",
                        "          return (TestRetainedDataFragment) retainedFragment;",
                        "        }",
                        "      }",
                        "    }",
                        "    return null;",
                        "  }",
                        "",
                        "  public static class TestRetainedDataFragment extends Fragment {",
                        "    Object thing;",
                        "",
                        "    boolean hasBeenRetained;",
                        "",
                        "    public TestRetainedDataFragment() {",
                        "      setRetainInstance(true);}",
                        "  }",
                        "}"
                ));

        ASSERT.about(javaSource()).that(source)
                .processedWith(new LeashAnnotationProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSource);
    }

    @Test
    public void testProcessSupportFragment() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
                "package test;",
                "import android.support.v4.app.Fragment;",
                "import com.rgeldmacher.leash.Retain;",
                "public class Test extends Fragment {",
                "    @Retain",
                "    Object thing;",
                "}"
        ));

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestLeash",
                Joiner.on('\n').join(
                        "package test;",
                        "import android.support.v4.app.Fragment;",
                        "import android.support.v4.app.FragmentActivity;",
                        "import android.support.v4.app.FragmentManager;",
                        "import java.lang.Object;",
                        "",
                        "public final class TestLeash {",
                        "  private TestLeash() {",
                        "  }",
                        "",
                        "  public static void restore(Test fragment) {",
                        "    FragmentActivity activity = null;",
                        "    if (fragment != null) {",
                        "      activity = fragment.getActivity();",
                        "    }",
                        "    TestRetainedDataFragment retainedFragment = getRetainedFragment(activity);",
                        "    if (retainedFragment != null) {",
                        "      if (retainedFragment.hasBeenRetained) {",
                        "        fragment.thing = retainedFragment.thing;",
                        "      }",
                        "    }",
                        "  }",
                        "",
                        "  public static void retain(Test fragment) {",
                        "    FragmentActivity activity = null;",
                        "    if (fragment != null) {",
                        "      activity = fragment.getActivity();",
                        "    }",
                        "    TestRetainedDataFragment retainedFragment = getRetainedFragment(activity);",
                        "    if (retainedFragment != null) {",
                        "      retainedFragment.thing = fragment.thing;",
                        "      retainedFragment.hasBeenRetained = true;",
                        "    }",
                        "  }",
                        "",
                        "  public static void clear(Test fragment) {",
                        "    FragmentActivity activity = null;",
                        "    if (fragment != null) {",
                        "      activity = fragment.getActivity();",
                        "    }",
                        "    TestRetainedDataFragment retainedFragment = getRetainedFragment(activity);",
                        "    if (retainedFragment != null) {",
                        "      retainedFragment.hasBeenRetained = false;",
                        "      retainedFragment.thing = null;",
                        "    }",
                        "  }",
                        "",
                        "  private static TestRetainedDataFragment getRetainedFragment(FragmentActivity activity) {",
                        "    if (activity != null) {",
                        "      FragmentManager fm = activity.getSupportFragmentManager();",
                        "      if (fm != null) {",
                        "        Fragment retainedFragment = fm.findFragmentByTag(\"TestRetainedDataFragment\");",
                        "        if (retainedFragment == null) {",
                        "          retainedFragment = new TestRetainedDataFragment();",
                        "          fm.beginTransaction().add(retainedFragment, \"TestRetainedDataFragment\").commit();",
                        "        }",
                        "        if (retainedFragment instanceof TestRetainedDataFragment) {",
                        "          return (TestRetainedDataFragment) retainedFragment;",
                        "        }",
                        "      }",
                        "    }",
                        "    return null;",
                        "  }",
                        "",
                        "  public static class TestRetainedDataFragment extends Fragment {",
                        "    Object thing;",
                        "",
                        "    boolean hasBeenRetained;",
                        "",
                        "    public TestRetainedDataFragment() {",
                        "      setRetainInstance(true);}",
                        "  }",
                        "}"
                ));

        ASSERT.about(javaSource()).that(source)
                .processedWith(new LeashAnnotationProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSource);
    }

    @Test
    public void testProcessPrimitives() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
                "package test;",
                "import android.app.Activity;",
                "import com.rgeldmacher.leash.Retain;",
                "",
                "public class Test extends Activity {",
                "",
                "    @Retain",
                "    boolean mBool;",
                "    @Retain",
                "    byte mByte;",
                "    @Retain",
                "    char mChar;",
                "    @Retain",
                "    double mDouble;",
                "    @Retain",
                "    float mFloat;",
                "    @Retain",
                "    int mInt;",
                "    @Retain",
                "    long mLong;",
                "    @Retain",
                "    short mShort;",
                "}"
        ));

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/TestLeash",
                Joiner.on('\n').join(
                        "package test;",
                        "import android.app.Activity;",
                        "import android.app.Fragment;",
                        "import android.app.FragmentManager;",
                        "",
                        "public final class TestLeash {",
                        "  private TestLeash() {",
                        "  }",
                        "",
                        "  public static void restore(Test activity) {",
                        "    TestRetainedDataFragment retainedFragment = getRetainedFragment(activity);",
                        "    if (retainedFragment != null) {",
                        "      if (retainedFragment.hasBeenRetained) {",
                        "        activity.mBool = retainedFragment.mBool;",
                        "        activity.mByte = retainedFragment.mByte;",
                        "        activity.mChar = retainedFragment.mChar;",
                        "        activity.mDouble = retainedFragment.mDouble;",
                        "        activity.mFloat = retainedFragment.mFloat;",
                        "        activity.mInt = retainedFragment.mInt;",
                        "        activity.mLong = retainedFragment.mLong;",
                        "        activity.mShort = retainedFragment.mShort;",
                        "      }",
                        "    }",
                        "  }",
                        "",
                        "  public static void retain(Test activity) {",
                        "    TestRetainedDataFragment retainedFragment = getRetainedFragment(activity);",
                        "    if (retainedFragment != null) {",
                        "      retainedFragment.mBool = activity.mBool;",
                        "      retainedFragment.mByte = activity.mByte;",
                        "      retainedFragment.mChar = activity.mChar;",
                        "      retainedFragment.mDouble = activity.mDouble;",
                        "      retainedFragment.mFloat = activity.mFloat;",
                        "      retainedFragment.mInt = activity.mInt;",
                        "      retainedFragment.mLong = activity.mLong;",
                        "      retainedFragment.mShort = activity.mShort;",
                        "      retainedFragment.hasBeenRetained = true;",
                        "    }",
                        "  }",
                        "",
                        "  public static void clear(Test activity) {",
                        "    TestRetainedDataFragment retainedFragment = getRetainedFragment(activity);",
                        "    if (retainedFragment != null) {",
                        "      retainedFragment.hasBeenRetained = false;",
                        "      retainedFragment.mBool = false;",
                        "      retainedFragment.mByte = 0;",
                        "      retainedFragment.mChar = '\u0000';",
                        "      retainedFragment.mDouble = 0.0d;",
                        "      retainedFragment.mFloat = 0.0f;",
                        "      retainedFragment.mInt = 0;",
                        "      retainedFragment.mLong = 0L;",
                        "      retainedFragment.mShort = 0;",
                        "    }",
                        "  }",
                        "",
                        "  private static TestRetainedDataFragment getRetainedFragment(Activity activity) {",
                        "    if (activity != null) {",
                        "      FragmentManager fm = activity.getFragmentManager();",
                        "      if (fm != null) {",
                        "        Fragment retainedFragment = fm.findFragmentByTag(\"TestRetainedDataFragment\");",
                        "        if (retainedFragment == null) {",
                        "          retainedFragment = new TestRetainedDataFragment();",
                        "          fm.beginTransaction().add(retainedFragment, \"TestRetainedDataFragment\").commit();",
                        "        }",
                        "        if (retainedFragment instanceof TestRetainedDataFragment) {",
                        "          return (TestRetainedDataFragment) retainedFragment;",
                        "        }",
                        "      }",
                        "    }",
                        "    return null;",
                        "  }",
                        "",
                        "  public static class TestRetainedDataFragment extends Fragment {",
                        "    boolean mBool;",
                        "    byte mByte;",
                        "    char mChar;",
                        "    double mDouble;",
                        "    float mFloat;",
                        "    int mInt;",
                        "    long mLong;",
                        "    short mShort;",
                        "    boolean hasBeenRetained;",
                        "",
                        "    public TestRetainedDataFragment() {",
                        "      setRetainInstance(true);}",
                        "  }",
                        "}"
                ));

        ASSERT.about(javaSource()).that(source)
                .processedWith(new LeashAnnotationProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedSource);
    }

    @Test
    public void testProcessLeashSourceNoActivityOrFragment() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
                "package test;",
                "import com.rgeldmacher.leash.Retain;",
                "public class Test {",
                "    @Retain",
                "    Object thing;",
                "}"
        ));

        ASSERT.about(javaSource()).that(source)
                .processedWith(new LeashAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("The @Retain annotation can only be applied to fields of an Activity or Fragment")
                .in(source).onLine(5);
    }

    @Test
    public void testProcessFieldIsPrivate() {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", Joiner.on('\n').join(
                "package test;",
                "import android.app.Activity;",
                "import com.rgeldmacher.leash.Retain;",
                "public class Test extends Activity {",
                "    @Retain",
                "    private Object thing;",
                "}"
        ));

        ASSERT.about(javaSource()).that(source)
                .processedWith(new LeashAnnotationProcessor())
                .failsToCompile()
                .withErrorContaining("Field must not be private, protected, static or final")
                .in(source).onLine(6);
    }
}
