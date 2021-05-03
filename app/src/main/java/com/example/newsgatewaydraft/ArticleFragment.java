package com.example.newsgatewaydraft;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 */
public class ArticleFragment extends Fragment {

    public ArticleFragment() {
        // Required empty public constructor
    }


    //Remember, the fragment is a single page in a swipeview
    //Each page will have the article content, and at the bottom,
    //The current page number and the total page number
    static ArticleFragment newInstance(Article article, int index, int max)
    {
        ArticleFragment f = new ArticleFragment();
        //Put data passed into fragment in a bundle to use in the oncreate for the fragment view
        Bundle bdl = new Bundle(1);
        bdl.putSerializable("ARTICLE_DATA", article);
        bdl.putSerializable("INDEX", index);
        bdl.putSerializable("TOTAL_COUNT", max);
        f.setArguments(bdl);
        return f;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //Specifies the layout the fragment will be on
        View fragment_layout = inflater.inflate(R.layout.fragment_article, container, false);


        //Gets the arguments passed in when the fragment was created, which were the article information,
        //page number, and total pages
        Bundle args = getArguments();
        if (args != null) {
            final Article currentArticle = (Article) args.getSerializable("ARTICLE_DATA");
            if (currentArticle == null) {
                return null;
            }
            int index = args.getInt("INDEX");
            int total = args.getInt("TOTAL_COUNT");


            TextView title = fragment_layout.findViewById(R.id.articleTitle);
            title.setText(currentArticle.getTitle());
            //Setting onclick listener for title
            title.setOnClickListener(v -> clickFlag(currentArticle.getURL()));


            //Change this one to accurately reflect time
            TextView publishedTime = fragment_layout.findViewById(R.id.timeStamp);
            publishedTime.setText(currentArticle.getPublishedAt());


            TextView author = fragment_layout.findViewById(R.id.author);
            author.setText(currentArticle.getAuthor());


            TextView description = fragment_layout.findViewById(R.id.articleDescription);
            description.setText(currentArticle.getDescription());
            //Setting onclick listener for description
            description.setOnClickListener(v -> clickFlag(currentArticle.getURL()));




            //Displays current and total pages at bottom of the screen
            TextView pagesDisplay = fragment_layout.findViewById(R.id.totalPages);
            pagesDisplay.setText(Integer.toString(index) + " of " + Integer.toString(total));



            ImageView imageView = fragment_layout.findViewById(R.id.imageView);
            /*
            imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

            imageView.setImageDrawable(currentCountry.getDrawable());
            imageView.setOnClickListener(v -> clickFlag(currentCountry.getName()));
            */

            final String imageurl = currentArticle.getUrlToImage();

            Picasso picasso = new Picasso.Builder(this.getContext()).listener(new Picasso.Listener()
            {
                @Override
                public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception)
                {
                    final String changedUrl = imageurl.replace("http:", "https:");
                    picasso.load(changedUrl).error(R.drawable.brokenimage)
                            .placeholder(R.drawable.placeholder).into(imageView);
                }
            }).build();
            picasso.load(imageurl).error(R.drawable.brokenimage)
                    .placeholder(R.drawable.placeholder).into(imageView);
            imageView.setOnClickListener(v -> clickFlag(currentArticle.getURL()));


            return fragment_layout;

        }

        else {
            return null;
        }
    }

    //Change this to open website rather than map
    private void clickFlag(String link) {

        Intent i = new Intent(Intent.ACTION_VIEW);

        i.setData(Uri.parse(link));
        startActivity(i);

    }

}
