package com.rgeldmacher.leash.sample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.rgeldmacher.leash.Leash;
import com.rgeldmacher.leash.Retain;


public class ExampleSupportActivity extends AppCompatActivity {

    ActivityTestHolder lostCount = new ActivityTestHolder();

    @Retain
    ActivityTestHolder retainedCount = new ActivityTestHolder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // restore retained objects
        Leash.restore(this);

        setContentView(R.layout.activity_support_example);

        final TextView lostCountView = (TextView) findViewById(R.id.count);
        final TextView retainedCountView = (TextView) findViewById(R.id.count2);
        findViewById(R.id.inc_button).setOnClickListener(new View.OnClickListener() {
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // store objects to be retained
        Leash.retain(this);
    }

    public static class ActivityTestHolder {

        int count;

    }
}
