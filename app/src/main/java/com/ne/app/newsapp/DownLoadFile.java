package com.ne.app.newsapp;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by ashrafiqubal on 16/01/16.
 */
public class DownLoadFile extends AsyncTask<String,Void,Boolean> {
    MainActivity mainActivity = new MainActivity();
    String newsName=null;
    private final String SONGPATH = "http://54.179.164.137:8080/AudiByteHindi/DATA/upload_news/";//This is the location in server where news is located with his name
    final private String DATAPATHLOCAL = Environment.getExternalStorageDirectory()+"/.AudiByte/.DATA";
    @Override
    protected Boolean doInBackground(String... params){
        Boolean prepared;
        newsName=params[0];
        try{
            Log.d("DownLoadFile:// ", params[0]);
            File folder = new File(DATAPATHLOCAL);
            String pathTemp = folder + "/" + params[0];
            File mediaExists = new File(pathTemp);
            if(!mediaExists.exists()) {
                String urlNews = SONGPATH+"/"+params[0];
                OutputStream outputStream;
                URL url = new URL(urlNews);
                Log.d("DownLoadFile:// ", urlNews + "  is ready to download");
                URLConnection connection = url.openConnection();
                connection.connect();
                InputStream inputStream = new BufferedInputStream(url.openStream());
                outputStream = new FileOutputStream(pathTemp);
                Log.d("DownLoadFile:// ", params[0] + " is ready to download");
                byte data[] = new byte[1024];
                int current = 0;
                while((current=inputStream.read(data)) != -1){
                    //Log.d("DownLoadFile:// ", params[0] + "  downloaded partially");
                    outputStream.write(data, 0, current);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();
            }
            Log.d("DownLoadFile:// ", params[0] + " is downloaded");
        }catch (Exception e){
            //MainActivity.getInstance().trackException(e);
            Log.d("DownLoadFile:// ","Error:// "+ e.getMessage());
            File folder = new File(DATAPATHLOCAL);
            String pathTemp = folder + "/" + params[0];
            File mediaExists = new File(pathTemp);
            if(mediaExists.exists()) {
                Log.d("SplashScreen:// ","onDestroy://" +params[0]);
                mediaExists.delete();
            }
            prepared = false;
            return prepared;
        }
        prepared = true;
        return prepared;
    }
    @Override
    protected void onPostExecute(Boolean result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
        Log.d("DownLoadFile:// ", "onPostExecution ");
        mainActivity.notifiactionFromDownLoadFile(newsName);
    }
}