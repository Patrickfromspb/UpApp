package com.gargolin.zello;

import android.app.Activity;
import android.widget.ProgressBar;

/**
 * Created by TraffPop on 2/13/2017.
 */

public class Running extends Thread {
    Activity activity;
    Running(Activity activity){
    this.activity=activity;
    }
    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                return;
            }

            ProgressBar progressBar = (ProgressBar) this.activity.findViewById(R.id.progressBar);
            progressBar.setProgress(progressBar.getProgress() + 1);
        }
    }
}
