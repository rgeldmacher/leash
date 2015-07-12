package com.rgeldmacher.leash.sample;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

/**
 * @author robertgeldmacher
 */
public class MainActivityLeashTemplate {

    static void getRetainedData(MainActivity activity) {
        MainActivityRetainedDataFragmentTemplate retainedFragment = getRetainedFragment(activity);
        if (retainedFragment != null) {
            if (retainedFragment.holder != null) {
                activity.lostCount = retainedFragment.holder;
            }
        }
    }

    static void retainData(MainActivity activity) {
        MainActivityRetainedDataFragmentTemplate retainedFragment = getRetainedFragment(activity);
        if (retainedFragment != null) {
            retainedFragment.holder = activity.lostCount;
        }
    }

    static MainActivityRetainedDataFragmentTemplate getRetainedFragment(FragmentActivity activity) {
        if (activity != null && activity.getSupportFragmentManager() != null) {
            FragmentManager fm = activity.getSupportFragmentManager();
            Fragment retainedFragment = fm.findFragmentByTag("_MainActivityRetainedDataFragment");
            if (retainedFragment == null) {
                retainedFragment = new MainActivityRetainedDataFragmentTemplate();
                fm.beginTransaction().add(retainedFragment, "_MainActivityRetainedDataFragment").commit();
            }

            if (retainedFragment instanceof MainActivityRetainedDataFragmentTemplate) {
                return (MainActivityRetainedDataFragmentTemplate) retainedFragment;
            }
        }

        return null;
    }


    public static class MainActivityRetainedDataFragmentTemplate extends Fragment {

        MainActivity.ActivityTestHolder holder;

        public MainActivityRetainedDataFragmentTemplate() {
            setRetainInstance(true);
        }
    }
}
