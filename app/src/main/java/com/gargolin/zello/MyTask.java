package com.gargolin.zello;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import org.apache.commons.io.FileUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifTextView;

import static com.gargolin.zello.MainActivity.filesDir;
import static com.gargolin.zello.MainActivity.musicFile;

/**
 * Created by TraffPop on 2/17/2017.
 */

class MyTask extends AsyncTask<Void, Void, Void> {

    public static File gifFile=null;
    OutputActivity outputActivity;
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
    MyTask(OutputActivity outputActivity){
        this.outputActivity=outputActivity;
    }
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
            gifFile = new File(filesDir+ "/needed.gif");
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
        if (serverResponseCode == 200) outputActivity.drawSuccess();
    }
}
