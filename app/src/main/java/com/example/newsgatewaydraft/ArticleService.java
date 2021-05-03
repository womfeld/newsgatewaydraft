package com.example.newsgatewaydraft;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.util.ArrayList;


//Have to add a receiver to this.  Because in main activity, we broadcast a request for
//all articles for a given news source

//The source receiver of article service receives this request,
// and loads the list of articles and then has the article service send the articles
//to the article receiver, which puts them into main activity


public class ArticleService extends Service {

    private SourceReceiver sourceReceiver;

    public static final String ACTION_MESSAGE_TO_SERVICE = "ACTION_MESSAGE_TO_SERVICE";
    public static final String NEWS_SOURCE_ID = "NEWS_SOURCE_ID";


    private static final String TAG = "ArticleService";
    public static final String ARTICLE_LIST_EXTRA = "ARTICLE_LIST_EXTRA";
    private boolean running = true;

    private ArrayList<Article> articles = new ArrayList<>();


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        //In the thread, we must wait for the article runnable to populate the articles arrayList.
        //Once this happens, we can then stop waiting and then broadcast the arrayList of articles to
        //the mainActivity

        //Creating new thread for the article service



        sourceReceiver = new SourceReceiver(this);
        IntentFilter sourceFilter = new IntentFilter(ACTION_MESSAGE_TO_SERVICE);
        registerReceiver(sourceReceiver, sourceFilter);









        new Thread(() -> {

            while (running) {


                while(articles.size()==0) {
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }


                //Once the articles list is populated, we then broadcast it to the mainActivity
                sendSvcBroadcast(articles);


            }

            // Send a final message
            //sendSvcBroadcast("Service Thread Stopped");

            Log.d(TAG, "run: Ending loop");
        }).start();






        return Service.START_NOT_STICKY;
    }


    public void setArticles(ArrayList<Article> aList) {

        articles.clear();

        articles.addAll(aList);

        //sendSvcBroadcast(articles);

        //t.setText(articles.get(0).getTitle());


    }


    //This sends a broadcast to the article receiver.
    //The broadcast contains the arrayList of articles downloaded from the article runnable
    private void sendSvcBroadcast(ArrayList<Article> aList) {
        Intent intent = new Intent();
        //Setting the intent action to this will allow the receiver to know that this broadcast
        //came from the articles service, as this is what we set the filter to in the main activity
        intent.setAction(MainActivity.ACTION_ARTICLES_FROM_SERVICE);
        intent.putExtra(ARTICLE_LIST_EXTRA, aList);
        sendBroadcast(intent);
        articles.clear();
    }

    @Override
    public void onDestroy() {
        // Send a message when destroyed
        //sendBroadcast("Service Destroyed");
        unregisterReceiver(sourceReceiver);
        running = false;
        super.onDestroy();
    }
}