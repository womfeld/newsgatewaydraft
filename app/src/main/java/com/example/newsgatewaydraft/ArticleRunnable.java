package com.example.newsgatewaydraft;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class ArticleRunnable implements Runnable {


    //Example URL
    //https://newsapi.org/v2/top-headlines?sources=cnn&language=en&apiKey=cac9bc5768d547209874208489e9e339

    private final String prefixURL = "https://newsapi.org/v2/top-headlines?";
    //private final String myAPIKey = "cac9bc5768d547209874208489e9e339";
    private final String myAPIKey = "63c267b58435414d84bbe4adf5f594ea";


    private static final String TAG = "ArticleRunnable";
    private final ArticleService articleService;
    private final String id;






    ArticleRunnable(ArticleService articleService, String id)
    {
        this.articleService = articleService;
        this.id = id;

    }


    @Override
    public void run() {


        Uri.Builder buildURL = Uri.parse(prefixURL).buildUpon();
        buildURL.appendQueryParameter("sources", id);
        buildURL.appendQueryParameter("language", "en");
        buildURL.appendQueryParameter("apiKey", myAPIKey);
        String urlToUse = buildURL.build().toString();

        Log.d(TAG, "test: " + urlToUse);


        StringBuilder sb = new StringBuilder();


        try {
            URL url = new URL(urlToUse);


            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.addRequestProperty("User-Agent","");
            conn.connect();

            if (conn.getResponseCode() != HttpsURLConnection.HTTP_OK) {
                Log.d(TAG, "run: HTTP ResponseCode NOT OK: " + conn.getResponseCode());
                processResults(null);
                return;
            }

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            Log.d(TAG, "run: " + sb.toString());

        } catch (Exception e) {
            Log.e(TAG, "run: ", e);
            processResults(null);
            return;
        }

        processResults(sb.toString());

    }



    private void processResults(String s) {
        final ArrayList<Article> aList = parseJSON(s);
        if (aList != null) {
            articleService.setArticles(aList);
        }
    }




    private ArrayList<Article> parseJSON(String s) {


        ArrayList<Article> articleList = new ArrayList<>();


        try {

            JSONObject jObjMain = new JSONObject(s);


            try {
                JSONArray articlesArray = jObjMain.getJSONArray("articles");


                //Must loop through the articles JSON Array, which stores all the political positions/jobs held
                for (int i = 0; i < articlesArray.length(); i++) {
                    JSONObject articleContent = (JSONObject) articlesArray.get(i);




                    String author = articleContent.getString("author");

                    String title = articleContent.getString("title");

                    String description = articleContent.getString("description");

                    String url = articleContent.getString("url");

                    String urlToImage = articleContent.getString("urlToImage");

                    String publishedAt = articleContent.getString("publishedAt");

                    //Format date
                    DateFormat dateFormat = new SimpleDateFormat("MM dd, yyyy HH:mm");
                    Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(publishedAt);
                    publishedAt = dateFormat.format(date);


                    articleList.add(new Article(author, title, description, url, urlToImage, publishedAt));


                }

            } catch (Exception e) {

                e.printStackTrace();
            }

            return articleList;


        } catch (Exception e) {

            e.printStackTrace();
        }



        return null;

    }




}
