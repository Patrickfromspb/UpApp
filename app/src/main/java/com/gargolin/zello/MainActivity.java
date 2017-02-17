package com.gargolin.zello;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifTextView;

import static android.R.attr.bitmap;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {
    File musicFile = null;
    Thread t;
    Button b1;
    private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
    private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private MediaRecorder recorder = null;
    private int currentFormat = 1;
    private int output_formats[] = {MediaRecorder.OutputFormat.MPEG_4, MediaRecorder.OutputFormat.THREE_GPP};
    private String file_exts[] = {AUDIO_RECORDER_FILE_EXT_MP4, AUDIO_RECORDER_FILE_EXT_3GP};

    /**
     * Called when the activity is first created.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation (ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                1);
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ((ProgressBar) findViewById(R.id.progressBar)).setLongClickable(true);
        ((ProgressBar) findViewById(R.id.progressBar)).setOnTouchListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                t = new Running(this);
                findViewById(R.id.recoding).setVisibility(View.VISIBLE);
                ((GifDrawable) (findViewById(R.id.recoding).getBackground())).start();
                t.start();
                startRecording();
                break;
            case MotionEvent.ACTION_UP:

                findViewById(R.id.recoding).setVisibility(View.GONE);
                t.interrupt();
                stopRecording();
                try {
                    Thread.sleep(120);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                break;
        }
        return false;
    }

    private String getFilename() {

        musicFile = new File(getFilesDir(), System.currentTimeMillis() + file_exts[currentFormat]);
        return musicFile.getAbsolutePath();

    }

    private void startRecording() {
        ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        recorder = new MediaRecorder();

        recorder.reset();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(output_formats[currentFormat]);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(getFilename());

        try {
            recorder.prepare();
            recorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void stopRecording() {
        if (null != recorder) {
            try {
                recorder.stop();
            } catch (RuntimeException e) {
            }
           findViewById(R.id.content_main).setBackgroundResource(R.drawable.images);
            findViewById(R.id.textView).setVisibility(View.GONE);
            recorder.reset();
            recorder.release();
            MyTask task = new MyTask();
            findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            findViewById(R.id.loading).setVisibility(View.VISIBLE);
            ((GifDrawable) (findViewById(R.id.loading).getBackground())).start();

            task.execute();
            recorder = null;


        }
    }

    class MyTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        DataInputStream inputStream = null;
        String pathToOurFile = musicFile.getAbsolutePath();
        String urlServer = "http://sergkolp8384.tmp.fstest.ru/handle_upload.php";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        File file;
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 100 * 1024 * 1024;
        int serverResponseCode = 0;

        @Override
        protected Void doInBackground(Void... params) {

            try {

                FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile));

                URL url = new URL(urlServer);
                connection = (HttpURLConnection) url.openConnection();

                // Allow Inputs &amp; Outputs.
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setUseCaches(false);

                // Set HTTP method to POST.
                connection.setRequestMethod("POST");

                connection.setRequestProperty("Connection", "Keep-Alive");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

                outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.writeBytes(twoHyphens + boundary + lineEnd);
                outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + pathToOurFile + "\"" + lineEnd);
                outputStream.writeBytes(lineEnd);

                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // Read file
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {
                    outputStream.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                outputStream.writeBytes(lineEnd);
                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);


                serverResponseCode = connection.getResponseCode();
                String serverResponseMessage = connection.getResponseMessage();


                System.out.println(serverResponseCode); // Should be 200*/
                connection.disconnect();
                URL urling = new URL("http://sergkolp8384.tmp.fstest.ru/needed.gif");
                URLConnection connection = urling.openConnection();
                connection.connect();
                file = new File(getFilesDir()+ "/needed.gif");
                if (file.exists()) file.delete();
                file.createNewFile();
                FileUtils.copyURLToFile(urling, file);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            musicFile.delete();
            if (serverResponseCode == 200) {
                ((ProgressBar) findViewById(R.id.progressBar)).setProgress(0);

                try {
                    findViewById(R.id.loading).setVisibility(View.INVISIBLE);
                    findViewById(R.id.applauds).setVisibility(View.VISIBLE);
                    ((GifTextView) findViewById(R.id.applauds)).setBackground(new GifDrawable(file));

                    ((GifDrawable) (findViewById(R.id.applauds).getBackground())).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
