package com.gargolin.zello;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;

import static com.gargolin.zello.MyTask.gifFile;

public class OutputActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output);
        MyTask task = new MyTask(this);
        findViewById(R.id.loading).setVisibility(View.VISIBLE);
        ((GifDrawable) (findViewById(R.id.loading).getBackground())).start();

        task.execute();

    }
    public void drawSuccess()
    {

            findViewById(R.id.loading).setVisibility(View.INVISIBLE);
            findViewById(R.id.applauds).setVisibility(View.VISIBLE);
        try {
            findViewById(R.id.applauds).setBackground(new GifDrawable(gifFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ((GifDrawable)findViewById(R.id.applauds).getBackground()).start();

    }
}
