package com.example.newsgatewaydraft;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class SourceRunnable implements Runnable {


    private final String prefixURL = "https://newsapi.org/v2/sources?language=en&country=us&category=";
    private final String myAPIKey = "cac9bc5768d547209874208489e9e339";


    private static final String TAG = "SourceRunnable";
    private final MainActivity mainActivity;


    //private HashSet<String> temps = new HashSet<>();
    //= new HashSet<String>();


    //Just added
    private HashMap<String, ArrayList<String>> newsMap = new HashMap<>();
    //Just added
    private HashMap<String, String> nameToIDMap = new HashMap<>();




    SourceRunnable(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;

    }



    @Override
    public void run() {

        Uri.Builder buildURL = Uri.parse(prefixURL).buildUpon();
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
        parseJSON(s);
        if (newsMap != null && nameToIDMap != null) {

            mainActivity.runOnUiThread(() -> mainActivity.setupSources(newsMap, nameToIDMap));

        }

    }

    //Fall back on
    /*
        private void processResults(String s) {
        //Fall back on
        final HashMap<String, ArrayList<String>> newsMap = parseJSON(s);
        if (newsMap != null) {
            //Fall back on
            mainActivity.runOnUiThread(() -> mainActivity.setupSources(newsMap));
        }
    }
     */




    //So use this one as reference... what we actually need to do is create a list of news sources and a list of news categories
    //based on the category input (it will either be nothing or a category selected from the options menu)

    //Nevermind again, I think this should be correct, may need to make minor changes though

    private void parseJSON(String s) {


        try {

            JSONObject jObjMain = new JSONObject(s);


            try {
                JSONArray sourcesArray = jObjMain.getJSONArray("sources");




                //Must loop through the sources JSON Array, which stores all the political positions/jobs held
                for (int i = 0; i < sourcesArray.length(); i++) {
                    JSONObject sourceContent = (JSONObject) sourcesArray.get(i);



                    //Get category, will be key of hashmap
                    String category = sourceContent.getString("category");

                    //Get source id
                    String id = sourceContent.getString("id");

                    //Get source name
                    String name = sourceContent.getString("name");




                    //When iterating through each JSON object in sources JSON array, will check whether the key for category is already
                    //in the hashmap or not
                    //If it is, append the name to the value (a hashset)
                    //If not, create a new key/value pair, the key being the category and the value being a hashset of one element (for now), the name


                    if (newsMap.containsKey(category)==true) {

                        newsMap.get(category).add(name);

                    }

                    else {

                        //temps.add(name);
                        //map.put(category, temps);

                        newsMap.put(category, new ArrayList<String>());
                        newsMap.get(category).add(name);

                    }

                    nameToIDMap.put(name, id);


                }

            } catch (Exception e) {

                e.printStackTrace();
            }



        } catch (Exception e) {

            e.printStackTrace();
        }

    }



    //Fall back on
/*

    //So use this one as reference... what we actually need to do is create a list of news sources and a list of news categories
    //based on the category input (it will either be nothing or a category selected from the options menu)

    //Nevermind again, I think this should be correct, may need to make minor changes though

    private HashMap<String, ArrayList<String>> parseJSON(String s) {

        HashMap<String, ArrayList<String>> map = new HashMap<>();


        try {

            JSONObject jObjMain = new JSONObject(s);


            try {
                JSONArray sourcesArray = jObjMain.getJSONArray("sources");




                //Must loop through the sources JSON Array, which stores all the political positions/jobs held
                for (int i = 0; i < sourcesArray.length(); i++) {
                    JSONObject sourceContent = (JSONObject) sourcesArray.get(i);



                    //Get category, will be key of hashmap
                    String category = sourceContent.getString("category");

                    //Get source id
                    String id = sourceContent.getString("id");

                    //Get source name
                    String name = sourceContent.getString("name");




                    //When iterating through each JSON object in sources JSON array, will check whether the key for category is already
                    //in the hashmap or not
                    //If it is, append the name to the value (a hashset)
                    //If not, create a new key/value pair, the key being the category and the value being a hashset of one element (for now), the name


                    if (map.containsKey(category)==true) {

                        map.get(category).add(name);

                    }

                    else {

                        //temps.add(name);
                        //map.put(category, temps);

                        map.put(category, new ArrayList<String>());
                        map.get(category).add(name);

                    }



                }

            } catch (Exception e) {

                e.printStackTrace();
            }

            return map;


        } catch (Exception e) {

            e.printStackTrace();
        }


        return null;
    }

 */



    //Try making another hashmap for name, id pairs



}
