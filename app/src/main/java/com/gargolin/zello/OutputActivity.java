package com.gargolin.zello;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import pl.droidsonroids.gif.GifDrawable;

public class OutputActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output);
        MyTask task = new MyTask(this.getBaseContext());
        findViewById(R.id.loading).setVisibility(View.VISIBLE);
        ((GifDrawable) (findViewById(R.id.loading).getBackground())).start();

        task.execute();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
