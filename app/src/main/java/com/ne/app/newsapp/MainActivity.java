package com.ne.app.newsapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.StandardExceptionParser;
import com.google.android.gms.analytics.Tracker;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    final int ID_IMAGE_BUTTON_PLAY = R.drawable.ic_play_circle_outline_temp;
    final int ID_IMAGE_BUTTON_PAUSE = R.drawable.ic_pause_circle_filled_temp;
    final int ID_IMAGE_BUTTON_PREV = R.drawable.ic_buttons_back_permanent;
    final int ID_IMAGE_BUTTON_PREV_TEMP = R.drawable.ic_buttons_back;
    final int ID_IMAGE_BUTTON_NEXT = R.drawable.ic_buttons_forward_permanent;
    final int ID_IMAGE_BUTTON_NEXT_TEMP = R.drawable.ic_buttons_forward_temp;
    final int ID_IMAGE_BUTTON_REPLAY = R.drawable.ic_replay_permanent;
    final int ID_IMAGE_BUTTON_REPLAY_TEMP=R.drawable.ic_replay_temp;
    final int OLD_FILE_DELETE_DAY = 2;//Day before which files are deleted
    final private String DATAPATHLOCAL = Environment.getExternalStorageDirectory() + "/.AudiByte/.DATA";
    public final String MAXNEWS="MAXNEWS";//This is the keyword used in Json String which contain Maximum news to play
    public final String TOP = "TOP";//This is the keyword used in json string which contain Top value
    public final static String SAVEDDATA = "SAVEDDATA";//This is used to open SharedPreference in private mode
    public final String SAVEDTOPVALUE="SAVEDTOPVALUE"; // This is the keyword contain Integer value used to calculate Saved top value in shared preference
    public static int top=2;//This integer is used to store the top value fatch from server
    public static int maxNewsToPlay=0;//This integer is used to store the Maximum News to play fatch from server
    public static int savedTopValue=0;// This integer is used to store the previous top value fatch from sharedPreference
    public static int reaminingNewNews = 0;//This integer is used to store the reamining new news
    public static int currentPlayingValue = -1;//This integer contain the current playing value
    public static String receivedJsonString = null;//This string is in Json form contain TOP, MAXNEWS, and name value pair of each news
    public static MediaPlayer mediaPlayer;//This the instance of media player
    public static boolean isDownloadingComplete=false;
    public static boolean playingBeforeCall=false;
    private static boolean shouldPlayByDefault=false;
    private static boolean isHaveToPlaySamacharSmapathHue = true;
    private static List<String> allFileName = new ArrayList<String>();//Array contain all news name in which latest news is at the end of array
    private Menu menu;
    private static final String GooglePlayStorePackageNameOld = "com.google.market";
    private static final String GooglePlayStorePackageNameNew = "com.android.vending";
    ImageButton btnPlayPause,playPrevNews,replayNews,playNextNews;
    TextView newNews,linkForMore;
    SharedPreferences sharedpreferences;
    static MainActivity mInstance;
    static Context parent;
    static Vibrator vibrator;
    public static boolean isNotified=false;//Used to determine whether notification is running or not
    //private static boolean isOffline = true;//recived the value from splash screen class
    private static int currentDownloadingValue=0;//it contain the value of downloaded file, it' value is decrease by one in notificationFromDownLoadFile
    private static List<String> downLoadedFileName = new ArrayList<String>();//Array contain name of all news which are downloaded to SD card
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Intent intent = getIntent();
        receivedJsonString = intent.getStringExtra(SplashScreen.JSON_STRING);
        //Log.d("MainActivity:// ", "onCreate://" + isOffline + "  " + receivedJsonString);
        initializeAllButton();
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        sharedpreferences = getSharedPreferences(SAVEDDATA, Context.MODE_PRIVATE);
        savedTopValue = sharedpreferences.getInt(SAVEDTOPVALUE, 0);
        mInstance = this;
        parent=this;
        AnalyticsTrackers.initialize(this);
        AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP);
        mediaPlayer = new MediaPlayer();
        spliteJsonString(receivedJsonString);
        deleteOldFile(OLD_FILE_DELETE_DAY);
        activateNotificationService();
        playNamaskar();
        setMediaCompleteLister();
        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        //updateLink("google.com","google");
    }
    @Override
    protected void onStart(){
        super.onStart();
        Log.d("MainActivity:// ", "onStart:// called");
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.d("MainActivity", "onResume:// Called ");
        if(isNotified){
            cancelNotification();
            isNotified=false;
        }
        trackScreenView("Home Screen:");
    }
    @Override
    public void onPause(){
        super.onPause();
        Log.d("MainActivity:// ", "onPause:// called ");
            if(!isNotified){
                updateNotification();
                isNotified=true;
            }
    }
    @Override
    protected void onStop(){
        super.onStop();
        Log.d("MainActivity:// ", "onStop:// called");
    }
    @Override
    protected void onRestart(){
        super.onRestart();
        Log.d("MainActivity:// ", "onRestart:// called");
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
        Log.d("MainActivity://", "onDestroy:// called ");
        if(isNotified){
            cancelNotification();
            isNotified=false;
        }
        //JSONObject obj=new JSONObject();
        //int downloadedTempSize = downLoadedFileName.size()-2;
        //obj.put(MAXNEWS, downloadedTempSize);
        //obj.put(TOP, savedTopValue);
        //int temp = top-downLoadedFileName.size();
        //for(int i=top,j=0; i>temp;i--,j++){
        //    obj.put("NEWS"+Integer.toString(i), downLoadedFileName.get(j));
        //    Log.d("MainActivity:// ","onDestroy:// " + downLoadedFileName.get(j) +" added");
        //}
        //String jSonString = obj.toJSONString();
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt(SAVEDTOPVALUE, savedTopValue);
        //editor.putString(SplashScreen.RECEIVEDJSONSTRING,jSonString);
        editor.commit();
        File folder = new File(DATAPATHLOCAL);
        String pathTemp = folder + "/" + allFileName.get(currentDownloadingValue);
        File mediaExists = new File(pathTemp);
        if (mediaExists.exists()) {
            Log.d("MainActivity:// ", "onDestroy://" + allFileName.get(currentDownloadingValue));
            mediaExists.delete();
        }
        // Stop method tracing that the activity started during onCreate()
        //android.os.Debug.stopMethodTracing();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
    @Override
    public void finish() {
        super.finish();
        Log.d("MainActivity://", "OnFinish called ");
        mediaPlayer.stop();
        mediaPlayer.release();
        if(isNotified){
            cancelNotification();
            isNotified=false;
        }
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt(SAVEDTOPVALUE, savedTopValue);
        editor.commit();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onStart();
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        this.menu = menu;
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.arrow_up:
                if(AppStatus.getInstance(getApplicationContext()).isOnline()){
                    Toast.makeText(getApplicationContext(),"No new news",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(),"No internet connection",Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.date:
                return true;
            case R.id.share:
                try{
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, "AudiByte- Bulletin in bits");
                    String sAux = "\nHi! Check out AudiByte app. I found it really nice for listening news \n";
                    sAux = sAux + "https://play.google.com/store/apps/details?id=com.ne.app.newsapp \n\n";
                    i.putExtra(Intent.EXTRA_TEXT, sAux);
                    startActivity(Intent.createChooser(i, "Share using:"));
                }
                catch(Exception e) {
                    Log.d("MainActivty:// ","OnOptionItemSelected:// "+e.getMessage());
                }
                return true;
            case R.id.rate_this_app:
                    if(isPlayStoreAvailable()){
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                    }else {
                        Toast.makeText(getApplicationContext(),"Play Store Not installed",Toast.LENGTH_SHORT).show();
                    }
            case R.id.exit:
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public boolean onPrepareOptionsMenu (Menu menu){
        MenuItem menuDate = menu.findItem(R.id.date);
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String formattedDate = df.format(c.getTime());
        menuDate.setTitle(formattedDate);
        return true;
    }
    /*@Override
    public void onBackPressed() {
            finish();
            Log.d("onBackButtonPressed://", "OnBackButtonPressed called ");
    }*/
    View.OnTouchListener chngNextNews = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action){
                case MotionEvent.ACTION_DOWN:
                    playNextNews.setImageResource(ID_IMAGE_BUTTON_NEXT_TEMP);
                    break;
                case MotionEvent.ACTION_OUTSIDE:
                    playNextNews.setImageResource(ID_IMAGE_BUTTON_NEXT);
                    break;
                case MotionEvent.ACTION_UP:
                    playNextNews.setImageResource(ID_IMAGE_BUTTON_NEXT);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    View.OnTouchListener chngPrevNews = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action){
                case MotionEvent.ACTION_DOWN:
                    playPrevNews.setImageResource(ID_IMAGE_BUTTON_PREV_TEMP);
                    break;
                case MotionEvent.ACTION_OUTSIDE:
                    playPrevNews.setImageResource(ID_IMAGE_BUTTON_PREV);
                    break;
                case MotionEvent.ACTION_UP:
                    playPrevNews.setImageResource(ID_IMAGE_BUTTON_PREV);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    View.OnTouchListener chngReplayNews = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            switch (action){
                case MotionEvent.ACTION_DOWN:
                    replayNews.setImageResource(ID_IMAGE_BUTTON_REPLAY_TEMP);
                    break;
                case MotionEvent.ACTION_OUTSIDE:
                    replayNews.setImageResource(ID_IMAGE_BUTTON_REPLAY);
                    break;
                case MotionEvent.ACTION_UP:
                    replayNews.setImageResource(ID_IMAGE_BUTTON_REPLAY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                default:
                    break;
            }
            return false;
        }
    };
    PhoneStateListener callStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber)
        {
            if(state==TelephonyManager.CALL_STATE_RINGING)
            {
                //Toast.makeText(getApplicationContext(),"Phone Is Riging", Toast.LENGTH_LONG).show();
                if(mediaPlayer.isPlaying()){
                    playingBeforeCall=true;
                    mediaPlayer.stop();
                }
            }
            // If incoming call received
            if(state==TelephonyManager.CALL_STATE_OFFHOOK)
            {
                //Toast.makeText(getApplicationContext(),"Phone is Currently in A call", Toast.LENGTH_LONG).show();
                if(mediaPlayer.isPlaying()){
                    playingBeforeCall=true;
                    mediaPlayer.stop();
                }
            }
            if(state==TelephonyManager.CALL_STATE_IDLE)
            {
                //Toast.makeText(getApplicationContext(),"phone is neither ringing nor in a call", Toast.LENGTH_LONG).show();
                if(playingBeforeCall){
                    if(!mediaPlayer.isPlaying()){
                        mediaPlayer.start();
                    }
                }
            }
        }
    };
    public void initializeAllButton(){
        btnPlayPause = (ImageButton)findViewById(R.id.playPause);
        playNextNews = (ImageButton)findViewById(R.id.playNextNews);
        playPrevNews = (ImageButton) findViewById(R.id.playPrevNews);
        replayNews = (ImageButton)findViewById(R.id.replayNews);
        playNextNews.setOnClickListener(playNext);
        playPrevNews.setOnClickListener(playPrev);
        btnPlayPause.setOnClickListener(playPauseFunction);
        playNextNews.setOnTouchListener(chngNextNews);
        playPrevNews.setOnTouchListener(chngPrevNews);
        replayNews.setOnTouchListener(chngReplayNews);
        newNews = (TextView)findViewById(R.id.newNews);
    }
    View.OnClickListener playNext = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            trackEvent("MainActivity: ", "Next news played", "Next news played");
            playNextNewsFunction();
        }
    };
    View.OnClickListener playPrev = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            trackEvent("MainActivity: ","Previous news played","Previous news played");
            playPrevNewsFunction();
        }
    };
    View.OnClickListener playPauseFunction = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
           playPause();
            trackEvent("MainActivity: ", "Play/Pause ", "Play/Paused");
        }
    };
    public static void updateNotification(){
        NotificationGenerator.customSimpleNotification(parent);
    }
    public void cancelNotification(){
        NotificationGenerator.cancelNotification();
    }
    public void playPrevNewsFunction(){
        shouldPlayByDefault=false;
        if(currentPlayingValue>0){
            mediaPlayer.stop();
            //mediaPlayer.reset();
            currentPlayingValue--;
            playThis(DATAPATHLOCAL + "/" + downLoadedFileName.get(currentPlayingValue));
            Log.d("MainActivity:// ", "playPrevNews1:// Playing prev value");
            setMediaCompleteLister();
        if(reaminingNewNews>0){
            reaminingNewNews=0;
            updateTextNewNews();
        }
        }else {
        Toast.makeText(getApplicationContext(),"You are listening latest news", Toast.LENGTH_SHORT).show();
        }
        isHaveToPlaySamacharSmapathHue = true;
    }
    public void playNextNewsFunction(){
        if(currentPlayingValue < downLoadedFileName.size() - 1) {
            mediaPlayer.stop();
            //mediaPlayer.reset();
            currentPlayingValue++;
            playThis(DATAPATHLOCAL + "/" + downLoadedFileName.get(currentPlayingValue));
            Log.d("MainActivity:// ", "PlayNextNews:// Playing next value");
            if(reaminingNewNews>0){
                reaminingNewNews--;
                updateTextNewNews();
            }
        }else if(currentPlayingValue<allFileName.size()-1){
            Log.d("MainActivity:// ", "PlayNextNews:// Please wait..! We are featching news for you");
            Toast.makeText(getApplicationContext(), "Please wait..! We are featching news for you", Toast.LENGTH_SHORT).show();
        }else {
            mediaPlayer.stop();
            //mediaPlayer.reset();
            playSamacharSmapathHue();
            Log.d("MainActivity:// ", "PlayNextNews:// You are done for now.");
            Toast.makeText(getApplicationContext(), "You are done for now.", Toast.LENGTH_SHORT).show();
        }
        setMediaCompleteLister();
    }
    public void replayNewsFunction(View view) {
        if(currentPlayingValue>=0){
            mediaPlayer.stop();
            //mediaPlayer.reset();
            playThis(DATAPATHLOCAL + "/" + downLoadedFileName.get(currentPlayingValue));
            trackEvent("MainActivity: ", "Replay news played", "Replay news played");
            Log.d("MainActivity:// ", "replayNews:// Reapting news");
        }
    }
    public void playPause(){
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }else {
            mediaPlayer.start();
        }
        updatePlayPauseButton();
    }
    private void updatePlayPauseButton() {
        if (mediaPlayer.isPlaying()){
            btnPlayPause.setImageResource(ID_IMAGE_BUTTON_PAUSE);
        }else {
            btnPlayPause.setImageResource(ID_IMAGE_BUTTON_PLAY);
        }
    }
    private void updateTextNewNews(){
        newNews.setText("New News: "+reaminingNewNews);
    }
    private void playThis(String dataSource){
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(dataSource);
            mediaPlayer.prepare();
            mediaPlayer.start();
            if(reaminingNewNews>0){
                updateTextNewNews();
            }
            updatePlayPauseButton();
            AnalyticsTrackers.initialize(this);
            AnalyticsTrackers.getInstance().get(AnalyticsTrackers.Target.APP);
            //btnPlayPause.setImageResource(R.drawable.buttons_pause_swell);
        }catch (IOException e){
            Log.d("MainActivity:// ", "playThis:// " + e.getMessage());
            trackException(e);
        }

    }
    public void spliteJsonString(String receivedJsonString){
        Object obj = JSONValue.parse(receivedJsonString);
        try {
            Log.d("MainActivity://", "spliteJsonString starts");
            JSONObject jsonObject = (JSONObject)obj;
            Long temp1= (Long)jsonObject.get(TOP);
            top=((int)(long)temp1);
            temp1 = (Long)jsonObject.get(MAXNEWS);
            maxNewsToPlay=((int)(long)temp1);
            int temp = top-maxNewsToPlay;
            reaminingNewNews = top-savedTopValue;
            if(reaminingNewNews>maxNewsToPlay){
                reaminingNewNews=maxNewsToPlay - 1;
            }
            if(reaminingNewNews<0){
                reaminingNewNews=0;
            }
            updateTextNewNews();
            reaminingNewNews++;
            savedTopValue=top;
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putInt(SAVEDTOPVALUE, savedTopValue);
            editor.commit();
            for(int i=temp+1 ; i<=top; i++) {
                String string = (String) jsonObject.get("NEWS" + Integer.toString(i));
                allFileName.add(string);
            }
            Log.d("MainActivity", "AllFileName is: " + allFileName.toString());
            currentDownloadingValue=allFileName.size()-1;
            downLoadedFileName.add(allFileName.get(currentDownloadingValue));
            currentDownloadingValue--;
            DownLoadFile downLoadFile = new DownLoadFile();
            downLoadFile.execute(allFileName.get(currentDownloadingValue));
        }catch (Exception e){
            trackException(e);
            Log.d("MainActivity:", "Exception occure " + e.getMessage());
        }
    }
    public void notifiactionFromDownLoadFile(String newsName){
        downLoadedFileName.add(newsName);
        if(shouldPlayByDefault){
            shouldPlayByDefault=false;
            currentPlayingValue=downLoadedFileName.size()-1;
            playThis(DATAPATHLOCAL + "/" + downLoadedFileName.get(currentPlayingValue));
        }
        if(currentDownloadingValue>0){
            currentDownloadingValue--;
            DownLoadFile downLoadFile = new DownLoadFile();
            downLoadFile.execute(allFileName.get(currentDownloadingValue));
        }else {
            isDownloadingComplete = true;
        }
    }
    public void deleteOldFile(int day){
        File folder = new File(DATAPATHLOCAL);
        Log.d("MainActivity://", "deleteOldFile:// " + folder.listFiles());
        for(File f : folder.listFiles()){
            long diff = new Date().getTime() - f.lastModified();
            Log.d("MainActivity://","deleteOldFile:// "+f);
            if (diff > day * 24 * 60 * 60 * 1000) {
                f.delete();
                Log.d("MainActivity://", "deleteOldFile:// " + f + " Deleted");
            }
        }
    }
    public static synchronized MainActivity getInstance() {
        return mInstance;
    }
    public synchronized Tracker getGoogleAnalyticsTracker() {
        AnalyticsTrackers analyticsTrackers = AnalyticsTrackers.getInstance();
        return analyticsTrackers.get(AnalyticsTrackers.Target.APP);
    }
    private void playNamaskar(){
        mediaPlayer = MediaPlayer.create(this,R.raw.namashkar);
        mediaPlayer.start();
        Log.d("MainActivity://", "PlayNamsakar called");
    }
    private void playSamacharSmapathHue(){
        mediaPlayer = MediaPlayer.create(this,R.raw.samachar_samapt_hue);
        mediaPlayer.start();
        isHaveToPlaySamacharSmapathHue =false;
        Log.d("MainActivity://", "PlaySamacharSamapathHue called");
    }
    private void setMediaCompleteLister(){
        try{
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    // TODO Auto-generated method stub
                    Log.d("MainActivity:// ", "playThis:// Playing next value");
                    if (currentPlayingValue < downLoadedFileName.size() - 1) {
                        mediaPlayer.stop();
                        //mediaPlayer.reset();
                        currentPlayingValue++;
                        playThis(DATAPATHLOCAL + "/" + downLoadedFileName.get(currentPlayingValue));
                        trackEvent("MainActivity: ", "Auto next news played", "Auto next news played");
                        Log.d("MainActivity:// ", "playThis:// Playing next value");
                        if (reaminingNewNews > 0) {
                            reaminingNewNews--;
                            updateTextNewNews();
                        }
                    } else if(currentPlayingValue<allFileName.size()-1){
                        Log.d("MainActivity:// ", "playThis:// Please wait..! We are featching news for you");
                        Toast.makeText(getApplicationContext(), "Please wait..! We are featching news for you", Toast.LENGTH_SHORT).show();
                        shouldPlayByDefault=true;
                    }else {
                        if(isHaveToPlaySamacharSmapathHue){
                            mediaPlayer.stop();
                            mediaPlayer.reset();
                            Log.d("MainActivity:// ", "playThis:// You are done for now.");
                            playSamacharSmapathHue();
                            setMediaCompleteLister();
                            Toast.makeText(getApplicationContext(), "You are done for now.", Toast.LENGTH_SHORT).show();
                        }else {
                            Log.d("MainActivity:// ", "playThis:// You are done for now_3.");
                            finish();
                        }
                    }
                }
            });
        }catch (Exception e){
            Log.d("MainActivity://", "onStart:// onCompleteListener");
        }
    }
    private void activateNotificationService(){
        Log.d("MainActivity:// ","activateNotificationService");
        ServiceConnection mConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className,
                                           IBinder binder) {
                ((KillNotificationService.KillBinder) binder).service.startService(new Intent(
                        MainActivity.this, KillNotificationService.class));
                Log.d("MainActivity:// ", "activateNotificationService:// onServiceConnected");
            }
            public void onServiceDisconnected(ComponentName className) {
                Log.d("MainActivity:// ", "activateNotificationService:// onServiceDisconnected");
            }
        };
        bindService(new Intent(MainActivity.this,
                        KillNotificationService.class), mConnection,
                Context.BIND_AUTO_CREATE);
    }
    public static void vibrate(int milliSeconds){
       vibrator.vibrate(milliSeconds);
    }
    public void updateLink(String link,String text){
        try{
            //linkForMore.setClickable(true);
            //linkForMore.setMovementMethod(LinkMovementMethod.getInstance());
            String url = "<a href='http://"+link+"'>"+text+"</a>";
           // linkForMore.setText(Html.fromHtml(url));
        }catch (Exception e){
            Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_SHORT).show();
        }
    }
    private boolean isPlayStoreAvailable(){
        PackageManager packageManager = getApplication().getPackageManager();
        List<PackageInfo> packages = packageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (PackageInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(GooglePlayStorePackageNameOld) ||
                    packageInfo.packageName.equals(GooglePlayStorePackageNameNew)) {
                    Log.d("MainActivity:// ", "isPlayStoreAvailable:// ");
                    return true;
            }
        }
        return false;
    }
    /***
     * Tracking screen view
     *
     * @param screenName screen name to be displayed on GA dashboard
     */
    public void trackScreenView(String screenName) {
        Tracker t = getGoogleAnalyticsTracker();

        // Set screen name.
        t.setScreenName(screenName);

        // Send a screen view.
        t.send(new HitBuilders.ScreenViewBuilder().build());

        GoogleAnalytics.getInstance(this).dispatchLocalHits();
    }

    /***
     * Tracking exception
     *
     * @param e exception to be tracked
     */
    public void trackException(Exception e) {
        if (e != null) {
            Tracker t = getGoogleAnalyticsTracker();

            t.send(new HitBuilders.ExceptionBuilder()
                            .setDescription(
                                    new StandardExceptionParser(this, null)
                                            .getDescription(Thread.currentThread().getName(), e))
                            .setFatal(false)
                            .build()
            );
        }
    }

    /***
     * Tracking event
     *
     * @param category event category
     * @param action   action of the event
     * @param label    label
     */
    public void trackEvent(String category, String action, String label) {
        Tracker t = getGoogleAnalyticsTracker();

        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder().setCategory(category).setAction(action).setLabel(label).build());
    }

}

