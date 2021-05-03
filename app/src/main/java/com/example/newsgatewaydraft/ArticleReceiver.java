package com.example.newsgatewaydraft;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;


//So text is set in sample receiver

class ArticleReceiver extends BroadcastReceiver {

    private static final String TAG = "ArticleReceiver";
    private final MainActivity mainActivity;

    public ArticleReceiver(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (action == null)
            return;

        //Remember, we set the filter for the receiver in the oncreate in mainactivity to articles_from_service
        //This means that anything anything broadcasted
        //from the service to be returned to mainactivity will have the action articles_from_service
        if (MainActivity.ACTION_ARTICLES_FROM_SERVICE.equals(action)) {

            ArrayList<Article> articles = new ArrayList<>();
            if (intent.hasExtra(ArticleService.ARTICLE_LIST_EXTRA)) {
                articles.addAll(intent.getParcelableArrayListExtra(ArticleService.ARTICLE_LIST_EXTRA));
            }


            //Fall back on
            /*
            //Textview that runnable data is going into
            ((TextView) mainActivity.findViewById(R.id.textView)).setText(data);

            //Just added
            //Sets both labels to first title returned from runnable article list
            ((TextView) mainActivity.findViewById(R.id.runnabletextView)).setText(data);
            */

            //Just added
            mainActivity.setupArticles(articles);


        } else {
            Log.d(TAG, "onReceive: Unknown broadcast received");
        }
    }
}
