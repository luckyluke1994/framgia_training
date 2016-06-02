package com.example.android.news.app.response_interface;

import com.example.android.news.app.instance.RSSItem;

import java.util.ArrayList;

/**
 * Created by lucky_luke on 4/8/2016.
 */
public interface AsyncResponse {
    void processFinish(ArrayList<RSSItem> output);
}
