package com.ne.app.newsapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

public class SplashScreen extends AppCompatActivity {
    private final static int CURRENTVERSION= 7;
    private static final int REQUEST_WRITE_STORAGE = 112;
    private final static String UPDATEDVERSION = "UPDATEDVERSION";
    public final String SONGPATH = "http://54.179.164.137:8080/AudiByteHindi/DATA/upload_news/";//This is the location in server where news is located with his name
    public final static String JSON_STRING = "com.ne.app.newsapp.JsonString";
    public final static String RECEIVEDJSONSTRING = "com.ne.app.newsapp.receivedJsonString";
    //public final static String ISOFFLINE = "com.ne.app.newsapp.isOffline";
    public final String DATAPATHLOCAL = Environment.getExternalStorageDirectory() + "/.AudiByte/.DATA";
    //public static boolean isHaveExternalStorage = true;
    private final String url = "http://54.179.164.137:8080/AudiByteHindi/GetAllNewsData?yes=6"; //This link is used to fatch the json String which contain Maximum new to play, Top value of news and Name of all news
    private static String receivedJsonString=null;
    private static int maxNews=0,topNew=0;
    private List<String> allFileName = new ArrayList<String>();//Array contain all news name in which latest news is at the end of array
    private final String MAXNEWS="MAXNEWS";//This is the keyword used in Json String which contain Maximum news to play
    private final String TOP = "TOP";//This is the keyword used in json string which contain Top value
    private static boolean haveToDeleteFile=false;
    static SharedPreferences sharedpreferences;
    private static int currentDownloadingValue=0;//it contain the value of downloaded file, it' value is decrease by one in notificationFromDownLoadFile
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("SplashScreen://", "OnCreate: executed");
        setContentView(R.layout.activity_splash_screen);
        boolean hasPermission = (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        }else {
            sharedpreferences = getSharedPreferences(MainActivity.SAVEDDATA, Context.MODE_PRIVATE);
            createFileAndFolder();
            if(AppStatus.getInstance(getApplicationContext()).isOnline()){
                GetAllNewsData getAllNewsData = new GetAllNewsData();
                getAllNewsData.execute(url);
                Log.d("SplashScreen://", "OnCreate: GetAllNewsData executed");
            }else{
                setContentView(R.layout.offline);
                Log.d("SplashScreen://", "OnCreate: No Internet ");
                Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
            }
        }

    }
    @Override
    protected void onStart(){
        super.onStart();
        Log.d("SplashScreen:// ", "onStart:// called");
    }
    @Override
    protected void onResume(){
        super.onResume();
        Log.d("SplashScreen:// ", "onResume:// called");
        //MainActivity.getInstance().trackScreenView("Splash screen");
    }
    @Override
    protected void onPause(){
        super.onPause();
        Log.d("SplashScreen:// ", "onPause:// called");
    }
    @Override
    protected void onStop(){
        super.onStop();
        Log.d("SplashScreen:// ", "onStop:// called");
    }
    @Override
    protected void onRestart(){
        super.onRestart();
        Log.d("SplashScreen:// ", "onRestart:// called");
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        Log.d("SplashScreen:// ", "onDestroy:// called");
        if(haveToDeleteFile){
            Log.d("SplashScreen:// ","onDestroy:// file deleted");
            File folder = new File(DATAPATHLOCAL);
            String pathTemp = folder + "/" + allFileName.get(currentDownloadingValue);
            File mediaExists = new File(pathTemp);
            if(mediaExists.exists()) {
                Log.d("SplashScreen:// ","onDestroy://" +allFileName.get(currentDownloadingValue));
                mediaExists.delete();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_WRITE_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    sharedpreferences = getSharedPreferences(MainActivity.SAVEDDATA, Context.MODE_PRIVATE);
                    createFileAndFolder();
                    if(AppStatus.getInstance(getApplicationContext()).isOnline()){
                        GetAllNewsData getAllNewsData = new GetAllNewsData();
                        getAllNewsData.execute(url);
                        Log.d("SplashScreen://", "OnCreate: GetAllNewsData executed");
                    }else{
                        setContentView(R.layout.offline);
                        Log.d("SplashScreen://", "OnCreate: No Internet ");
                        Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                    }
                    //reload my activity with permission granted or use the features what required the permission
                } else
                {
                    Toast.makeText(this, "The app was not allowed to write to your storage. Hence, it cannot function properly. Please consider granting it this permission", Toast.LENGTH_LONG).show();
                }
            }
        }

    }
    private void createFileAndFolder(){
        Log.d("SpalshScreen://", "createFileAndFolder() executed");
        try{
            File folder = new File(DATAPATHLOCAL);
            if (!folder.exists()) {
                folder.mkdirs();
                Log.d("SpalshScreen://", "Folder AudiByte/.DATA Created");
            }
            File noMedia = new File(DATAPATHLOCAL+"/.nomedia");
            if (!noMedia.exists()){
                noMedia.createNewFile();
                Log.d("SplashScreen:// ", ".nomedia created");
            }
        }catch (Exception e){
            //MainActivity.getInstance().trackException(e);
            Log.d("SplashScreen://","Error - "+e.getMessage());
        }
    }
    public void retryToFetch(View view){
        Log.d("SplashScreen:// ","retryToFetch executed");
        if(AppStatus.getInstance(getApplicationContext()).isOnline()){
            setContentView(R.layout.activity_splash_screen);
            GetAllNewsData getAllNewsData = new GetAllNewsData();
            getAllNewsData.execute(url);
        }else{
            Toast.makeText(getApplicationContext(),"Still, no internet connection",Toast.LENGTH_SHORT).show();
        }
    }
    public void updateVersion(View view){
        //Log.d("SplashScreen://", "updateVersion ");
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
    }
    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }
    public class GetAllNewsData extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub
            Boolean prepared;
            try {
                String str;
                HttpClient myClient = new DefaultHttpClient();
                HttpGet get = new HttpGet(params[0]);
                HttpResponse myResponse = myClient.execute(get);
                BufferedReader br = new BufferedReader(new InputStreamReader(myResponse.getEntity().getContent()));
                while ((str = br.readLine()) != null) {
                    receivedJsonString = str;
                    Log.d("GetAllFileName 1: ", str);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            prepared = true;
            return prepared;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            Log.d("GetAllFileName 1: ", "onPostExecution");
            Object obj = JSONValue.parse(receivedJsonString);
            try {
                Log.d("GetAllFileName", "OnPostExecution starts");
                JSONObject jsonObject = (JSONObject)obj;
                Long temp1= (Long)jsonObject.get(TOP);
                topNew=((int)(long)temp1);
                temp1 = (Long)jsonObject.get(MAXNEWS);
                maxNews=((int)(long)temp1);
                int temp = topNew-maxNews;
                for(int i=temp+1 ; i<=topNew;i++){
                    String string = (String)jsonObject.get("NEWS"+Integer.toString(i));
                    allFileName.add(string);
                }
                Log.d("GetAllFileName", "AllFileName is: " + allFileName.toString());
                temp1 = (Long)jsonObject.get(UPDATEDVERSION);
                int updatedVersion = ((int)(long)temp1);
                if(CURRENTVERSION<updatedVersion){
                    //Log.d("GetAllFileName 1: ",Integer.toString(CURRENTVERSION)+"  "+Integer.toString(updatedVersion));
                    setContentView(R.layout.update_version);
                }else {
                    currentDownloadingValue=allFileName.size()-1;
                    DownLoadFirstFile downLoadFile = new DownLoadFirstFile();
                    downLoadFile.execute(allFileName.get(currentDownloadingValue));
                    haveToDeleteFile=true;
                }
            }catch (Exception e){
                //MainActivity.getInstance().trackException(e);
                Log.d("GetAllFileName//", "Exception occure "+e.getMessage());
            }
        }
    }
    public class DownLoadFirstFile extends AsyncTask<String,Void,Boolean> {
        String newsName = null;
        @Override
        protected Boolean doInBackground(String... params) {
            Boolean prepared;
            newsName = params[0];
            try {
                //Log.d("DownLoadFile:// ", params[0]);
                File folder = new File(DATAPATHLOCAL);
                String pathTemp = folder + "/" + params[0];
                File mediaExists = new File(pathTemp);
                if (!mediaExists.exists()) {
                    String urlNews = SONGPATH + "/" + params[0];
                    OutputStream outputStream;
                    URL url = new URL(urlNews);
                    //Log.d("DownLoadFile:// ", urlNews + "  is ready to download");
                    URLConnection connection = url.openConnection();
                    connection.connect();
                    InputStream inputStream = new BufferedInputStream(url.openStream());
                    outputStream = new FileOutputStream(pathTemp);
                    //Log.d("DownLoadFile:// ", params[0] + " is ready to download");
                    byte data[] = new byte[1024];
                    int current = 0;
                    while ((current = inputStream.read(data)) != -1) {
                        outputStream.write(data, 0, current);
                    }
                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();
                }
                //Log.d("DownLoadFile:// ", params[0] + " is downloaded");
            } catch (Exception e) {
                //MainActivity.getInstance().trackException(e);
                //Log.d("DownLoadFile:// ", "Error:// indownload " + e.getMessage());
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
            try{
                //cancelTimer=true;
                haveToDeleteFile=false;
                Intent mainActivity = new Intent(SplashScreen.this,MainActivity.class);
                mainActivity.putExtra(JSON_STRING,receivedJsonString);
                startActivity(mainActivity);
                finish();
            }catch (Exception e){
                //MainActivity.getInstance().trackException(e);
                Log.d("SpalshScreen://","Error - "+e.getMessage());
            }
        }
    }
}
