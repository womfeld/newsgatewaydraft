package com.example.newsgatewaydraft;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    //Source names displayed in the drawer
    private final ArrayList<String> sourceNamesDisplayed = new ArrayList<>();


    //Hashmap with categories as keys and names of sources as values
    private final HashMap<String, ArrayList<String>> newsMap = new HashMap<>();


    //Just added////////////////////////
    private final HashMap<String, String> sourceNameIdMap = new HashMap<>();


    //Adapts name source data into drawer
    private ArrayAdapter<String> arrayAdapter;


    //Reference to menu because we need to dynamically fill in menu
    private Menu opt_menu;


    //Standard drawer layout stuff
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;



    //Adding service components
    //Action for intent that broadcasts the source id to the service
    public static final String ACTION_MESSAGE_TO_SERVICE = "ACTION_MESSAGE_TO_SERVICE";
    //Action for intent that broadcasts article objects list to the artcle receiver
    public static final String ACTION_ARTICLES_FROM_SERVICE = "ACTION_ARTICLES_FROM_SERVICE";
    //Key for source id extra
    public static final String NEWS_SOURCE_ID = "NEWS_SOURCE_ID";
    private ArticleReceiver serviceReceiver;
    private boolean serviceIsRunning = false;



    //List of fragments (i.e. when a news source is picked, we have to build the articles
    //because all articles that are displayed are instances of the fragment class
    //The instances of the article populate this list
    private List<Fragment> fragments;
    //Remember, we need an adapter to go from a list of something to
    //a visual list.  In this case, it's the list of fragments to the view pager
    private MyPageAdapter pageAdapter;



    private ViewPager pager;
    private String currentArticle;


    private SourceReceiver sourceReceiver;
    private ArticleService articleService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerList = findViewById(R.id.drawer_list);

        // Set up the drawer item click callback method
        //Called when something is clicked in drawer
        mDrawerList.setOnItemClickListener(
                (parent, view, position, id) -> {
                    selectItem(position);
                    mDrawerLayout.closeDrawer(mDrawerList);
                }
        );

        // Create the drawer toggle
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        );



        //Initialize the fragment list
        fragments = new ArrayList<>();



        //Sets up the adapter for the view pager
        pageAdapter = new MyPageAdapter(getSupportFragmentManager());
        pager = findViewById(R.id.viewpager);
        pager.setAdapter(pageAdapter);



        // Load the data
        if (newsMap.isEmpty()) {
            SourceRunnable rn = new SourceRunnable(this);
            new Thread(rn).start();
        }


        //What we need to do is start the article service so it is running and its receiver is registered
        //This allows the article service to wait for the broadcast signal sent when something from the drawer is clicked
        Intent serviceIntent = new Intent(MainActivity.this, ArticleService.class);
        startService(serviceIntent);


        //Initializes receiver so we can receive input from the article Service
        serviceReceiver = new ArticleReceiver(this);
        IntentFilter filter = new IntentFilter(ACTION_ARTICLES_FROM_SERVICE);
        registerReceiver(serviceReceiver, filter);



        //articleService = new ArticleService();
        //sourceReceiver = new SourceReceiver(articleService);
        //IntentFilter sourceFilter = new IntentFilter(ACTION_MESSAGE_TO_SERVICE);
        //registerReceiver(sourceReceiver, sourceFilter);

    }





    //Methods for interacting with the service to generate the articles





    //Possibly replace with broadcast
    private void broadcastServiceWithID(String s) {
        //Intent intent = new Intent(MainActivity.this, ArticleService.class);
        Intent intent = new Intent();
        intent.setAction(ACTION_MESSAGE_TO_SERVICE);
        intent.putExtra(NEWS_SOURCE_ID, s);
        //startService(intent);
        sendBroadcast(intent);
        serviceIsRunning = true;
        //textView.setText(R.string.running);
    }


    private void stopService() {
        Intent intent = new Intent(MainActivity.this, ArticleService.class);
        stopService(intent);
        //serviceisrunning = false
        //textView.setText(R.string.not_running);
    }


    @Override
    protected void onDestroy() {
        // Unregister your receiver
        unregisterReceiver(serviceReceiver);

        // Stop the service
        stopService();
        super.onDestroy();
    }




    //Gets data for options menu (categories) and drawer (sources)
    //Gets hashmap passed back from runnable
    public void setupSources(HashMap<String, ArrayList<String>> categoryMapIn, HashMap<String, String> nameMapIn) {

        sourceNameIdMap.clear();

        for (String s : nameMapIn.keySet()) {
            String id = nameMapIn.get(s);
            if (id == null)
                continue;

            sourceNameIdMap.put(s, id);
        }




        newsMap.clear();


        //Add all keys/values in categoryMapIn Hashmap to newsMap HashMap

        for (String s : categoryMapIn.keySet()) {
            ArrayList<String> sourceNames = categoryMapIn.get(s);
            if (sourceNames == null)
                continue;
            Collections.sort(sourceNames);
            newsMap.put(s, sourceNames);
        }

        //If we started with arraylist as value in runnable rather than hashset, no need to go through extra
        //trouble above
        ArrayList<String> tempList = new ArrayList<>(newsMap.keySet());


        //Adds the "all" categories option to tempList
        tempList.add("all");


        //Add all categories to options menu
        Collections.sort(tempList);
        for (String s : tempList)
            opt_menu.add(s);



        //Populates drawer with all possible news sources


        //First, get all possible source values from each arrayList in the newsMap hashmap
        //There will be duplicates, so then after we get all source names, remove the duplicates
        //then add it to the sourceNamesDisplayed list and then set the drawer entries to the
        //values in the sourceNamesDisplayed list

        ArrayList<String> sourcesWithRepeats = new ArrayList<>();

        for (String s : categoryMapIn.keySet()) {
            ArrayList<String> tmpSources = categoryMapIn.get(s);
            if (tmpSources == null)
                continue;
            sourcesWithRepeats.addAll(tmpSources);
        }


        //Removes duplicates from the source list then adds the new list to the sources to be displayed list
        Set<String> set = new HashSet<>(sourcesWithRepeats);
        sourceNamesDisplayed.addAll(set);
        Collections.sort(sourceNamesDisplayed);





        //Sets up adapter for drawer with the source names list then adds all the source names to the drawer
        arrayAdapter = new ArrayAdapter<>(this, R.layout.drawer_item, sourceNamesDisplayed);
        mDrawerList.setAdapter(arrayAdapter);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    




    //Method called when an item in the drawer is selected
    private void selectItem(int position) {

        pager.setBackground(null);
        currentArticle = sourceNamesDisplayed.get(position);
        //Just added
        String id = sourceNameIdMap.get(currentArticle);


        //Fall back on
        //ArticleRunnable rnn = new ArticleRunnable(this, id);
        //new Thread(rnn).start();


        //Starts the article service given the id for the source
        //Fall back on
        broadcastServiceWithID(id);


        mDrawerLayout.closeDrawer(mDrawerList);

    }



    //Gets the data for the Articles
    //Receives an arrayList of articles and then puts it into the fragment list and notifies the
    //pageAdapter of a data chagne

    //Shouldn't matter if this is called from the runnable or the service receiver ///////////////////// DELETE COMMENT LATER
    public void setupArticles(ArrayList<Article> aList) {

        //Two main tasks in this method:
        //Put the articles returned from runnable in the fragment list
        //Connect the fragment list to the pager adapter


        setTitle(currentArticle);

        for (int i = 0; i < pageAdapter.getCount(); i++)
            pageAdapter.notifyChangeInPosition(i);


        fragments.clear();


        //The below code initializes the list of fragments that will be connected with the view
        //pager via the pager adapter


        //Adds each entry in the article list to the list of fragments
        //Also adds the index and size of the list to the fragment (this must be less than
        //or equal to 10)

        int limit;
        if (aList.size()<10) {
            limit = aList.size();
        }
        else {
            limit = 10;
        }

        for (int i = 0; i < limit; i++) {
            fragments.add(
                    ArticleFragment.newInstance(aList.get(i), i+1, limit));
            //pageAdapter.notifyChangeInPosition(i);
        }

        //Notify adapter dataset has been changed
        pageAdapter.notifyDataSetChanged();
        //Make sure pager starts at the first item again
        pager.setCurrentItem(0);

    }


    // You need the 2 below to make the drawer-toggle work properly:

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }



    // You need the below to open the drawer when the toggle is clicked
    // Same method is called when an options menu item is selected.

    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            Log.d(TAG, "onOptionsItemSelected: mDrawerToggle " + item);
            return true;
        }

        setTitle(item.getTitle());




        //When a category is clicked, we must load all sources for that category
        //That is what the below code does
        sourceNamesDisplayed.clear();



        //Gets the category string that was selected, then gets all source names for that category and puts them into a list
        ArrayList<String> lst = newsMap.get(item.getTitle().toString());
        if (lst != null) {
            sourceNamesDisplayed.addAll(lst);
        }

        //It is possible that the "all" category was selected
        //Because this was not parsed from JSON data, we need to hardcode what is displayed in drawer if "all" is selected
        else {

            if (item.getTitle().toString().equals("all")) {
                //Displays all possible sources if "all" option is selected
                ArrayList allSources = new ArrayList<>(sourceNameIdMap.keySet());
                Collections.sort(allSources);
                sourceNamesDisplayed.addAll(allSources);
            }

        }



        arrayAdapter.notifyDataSetChanged();
        return super.onOptionsItemSelected(item);

    }

    // You need this to set up the options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        opt_menu = menu;
        return true;
    }




    //////////////////////////////////////


    //Adapter for the view pager
    //Quick recap (check this): view pager is the
    //whole swipe view, and each page in the swipe view is a
    //fragment
    //A page adapter is used to populate the fragment list into
    //the view pager

    //This is similar to a recycler view: each entry in a
    //recycler view (in a view pager, each entry is a page) containts
    // regular objects(rather than fragments) and a recyclerview adapter
    // is used to populate each recyclerview entry with those objects

    private class MyPageAdapter extends FragmentPagerAdapter {
        private long baseId = 0;


        MyPageAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        //Allows us to dynamically create the pager
        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        //Allows us to populate fragment list
        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public long getItemId(int position) {
            // give an ID different from position when position has been changed
            return baseId + position;
        }

        /**
         * Notify that the position of a fragment has been changed.
         * Create a new ID for each position to force recreation of the fragment
         * @param n number of items which have been changed
         */
        void notifyChangeInPosition(int n) {
            // shift the ID returned by getItemId outside the range of all previous fragments
            baseId += getCount() + n;
        }

    }

}