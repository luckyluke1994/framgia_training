package com.example.android.news.app.instance;

/**
 * Created by lucky_luke on 4/8/2016.
 */
public class RSSItem {

    private String mTitle;
    private String mDescription;
    private String mDate;
    private String mImage;
    private String mLink;

    public void RSSItem() {
        this.mTitle       = null;
        this.mDescription = null;
        this.mDate        = null;
        this.mImage       = null;
        this.mLink        = null;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public void setImage(String image) {
        mImage = image;
    }

    public void setLink(String link) {
        mLink = link;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getDate() {
        return mDate;
    }

    public String getImage() {
        return mImage;
    }

    public String getLink() {
        return mLink;
    }
}
