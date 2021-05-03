package com.example.newsgatewaydraft;

import java.io.Serializable;

public class Article implements Serializable {


    private final String author;
    private final String title;
    private final String description;
    private final String url;
    private final String urlToImage;
    private final String publishedAt;


    public Article(String author, String title, String description, String url, String urlToImage, String publishedAt) {

        this.author = author;
        this.title = title;
        this.description = description;
        this.url = url;
        this.urlToImage = urlToImage;
        this.publishedAt = publishedAt;


    }


    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getURL() {
        return url;
    }

    public String getUrlToImage() {
        return urlToImage;
    }

    public String getPublishedAt() {
        return publishedAt;
    }



}
