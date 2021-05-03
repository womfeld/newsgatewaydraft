package com.example.newsgatewaydraft;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;


//This sends the source id from the mainactivity to the source receiver in the article service, which we can
//then call the article loader runnable with
public class SourceReceiver extends BroadcastReceiver {

    private static final String TAG = "SourceReceiver";
    private final ArticleService articleService;

    public SourceReceiver(ArticleService articleService) {
        this.articleService = articleService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (action == null)
            return;

        //Remember, we set the filter for the receiver in the oncreate in mainactivity to articles_from_service
        //This means that anything anything broadcasted
        //from the service to be returned to mainactivity will have the action articles_from_service

        if (MainActivity.ACTION_MESSAGE_TO_SERVICE.equals(action)) {

            Log.d(TAG, "WE GOT HERE");

            String id = "";
            if (intent.hasExtra(MainActivity.NEWS_SOURCE_ID)) {
                id = intent.getStringExtra(MainActivity.NEWS_SOURCE_ID);
            }



            //Initiates the articles runnable
            ArticleRunnable dum = new ArticleRunnable(articleService, id);
            Thread dmm = new Thread(dum);
            dmm.start();


        } else {
            Log.d(TAG, "onReceive: Unknown broadcast received");
        }
    }
}