package com.rgeldmacher.leash.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rgeldmacher.leash.annotation.Retain;


public class MainFragment extends Fragment {

    FragmentTestHolder lostCount = new FragmentTestHolder();

    @Retain
    FragmentTestHolder retainedCount = new FragmentTestHolder();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainFragmentLeash.getRetainedData(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        MainFragmentLeash.retainData(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TextView lostCountView = (TextView) view.findViewById(R.id.fragment_count);
        final TextView retainedCountView = (TextView) view.findViewById(R.id.fragment_count2);

        view.findViewById(R.id.fragment_inc_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lostCount.count++;
                retainedCount.count++;

                lostCountView.setText("Lost Count: " + lostCount.count);
                retainedCountView.setText("Retained Count: " + retainedCount.count);
            }
        });

        lostCountView.setText("Lost Count: " + lostCount.count);
        retainedCountView.setText("Retained Count: " + retainedCount.count);
    }

    public static class FragmentTestHolder {

        int count;
    }
}