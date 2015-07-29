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

package com.rgeldmacher.leash.sample;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Build;
import android.os.Bundle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.util.ActivityController;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author robertgeldmacher
 */
@RunWith(RobolectricGradleTestRunner.class) //
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.KITKAT, shadows = {ExampleActivityTest.ExampleActivityShadow.class})
public class ExampleActivityTest {

    @Mock
    private FragmentManager fragmentManagerMock;

    @Before
    public void setup() {
        initMocks(this);
        setupFragmentManagerMock();
    }

    @Test
    public void testRestoreAfterConfigurationChange() {
        // prepare
        ActivityController<ExampleActivity> controller = Robolectric.buildActivity(ExampleActivity.class);
        ExampleActivity activity = controller.get();
        ExampleActivityShadow shadow = (ExampleActivityShadow) Shadows.shadowOf(activity);
        shadow.setFragmentManager(fragmentManagerMock);

        ActivityController<ExampleActivity> controller2 = Robolectric.buildActivity(ExampleActivity.class);
        ExampleActivity recreatedActivity = controller2.get();
        ExampleActivityShadow recreatedActivityShadow = (ExampleActivityShadow) Shadows.shadowOf(recreatedActivity);
        recreatedActivityShadow.setFragmentManager(fragmentManagerMock);

        // run & verify
        controller.create().start().resume().visible();

        activity.findViewById(R.id.inc_button).performClick();
        activity.findViewById(R.id.inc_button).performClick();

        assertEquals(2, activity.lostCount.count);
        assertEquals(2, activity.retainedCount.count);

        Bundle bundle = new Bundle();
        controller.saveInstanceState(bundle).pause().stop().destroy();
        controller2.create(bundle).start().restoreInstanceState(bundle).resume().visible();

        assertEquals(0, recreatedActivity.lostCount.count);
        assertEquals(2, recreatedActivity.retainedCount.count);
    }

    private void setupFragmentManagerMock() {
        final HashMap<String, Fragment> fragments = new HashMap<>();
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return fragments.get(invocation.getArguments()[0]);
            }
        }).when(fragmentManagerMock).findFragmentByTag(anyString());

        final HashMap<String, Fragment> fragmentsToBeAdded = new HashMap<>();
        final FragmentTransaction fragmentTransactionMock = mock(FragmentTransaction.class);
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                fragmentsToBeAdded.put((String) invocation.getArguments()[1], (Fragment) invocation.getArguments()[0]);
                return fragmentTransactionMock;
            }
        }).when(fragmentTransactionMock).add(any(Fragment.class), anyString());
        doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                fragments.putAll(fragmentsToBeAdded);
                return null;
            }
        }).when(fragmentTransactionMock).commit();

        when(fragmentManagerMock.beginTransaction()).thenReturn(fragmentTransactionMock);
    }

    @Implements(Activity.class)
    public static class ExampleActivityShadow extends ShadowActivity {

        private FragmentManager fragmentManager;

        @Implementation
        public FragmentManager getFragmentManager() {
            return fragmentManager;
        }

        public void setFragmentManager(FragmentManager fragmentManager) {
            this.fragmentManager = fragmentManager;
        }
    }
}
