package com.rgeldmacher.leash.sample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.rgeldmacher.leash.Leash;
import com.rgeldmacher.leash.Retain;


public class ExampleActivity extends Activity {

    ActivityTestHolder lostCount = new ActivityTestHolder();

    @Retain
    ActivityTestHolder retainedCount = new ActivityTestHolder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Leash.restore(this);

        setContentView(R.layout.activity_example);

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

        findViewById(R.id.support_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ExampleActivity.this, ExampleSupportActivity.class));
            }
        });
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
