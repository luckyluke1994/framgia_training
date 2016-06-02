package com.example.android.news.app;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.news.app.database.NewsContract;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class DetailActivity extends AppCompatActivity {
    private TextView mTitleTextView;
    private ImageView mThumImage;
    private TextView mContentTextView;
    private WebView mWebView;
    private ProgressBar mProgressBar;
    private loadData mAsysncTask;
    private String mLink;
    private String mContent;

    private static final String[] DETAIL_COLUMNS = {
            NewsContract.DetailEntry.TABLE_NAME + "." + NewsContract.DetailEntry._ID,
            NewsContract.DetailEntry.COLUMN_LINK,
            NewsContract.DetailEntry.COLUMN_CONTENT
    };

    private static final int COL_DETAIL_ID = 0;
    private static final int COL_DETAIL_LINK = 1;
    private static final int COL_DETAIL_CONTENT = 2;


    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences pref = getSharedPreferences(SettingsActivity.NewsPref, Context.MODE_PRIVATE);
        boolean isTrue = pref.getBoolean(SettingsActivity.MODE_NETWORK, true);

        String uri = "@drawable/stub";
        int imageResource = getResources().getIdentifier(uri, null, getPackageName());
        Drawable res = getResources().getDrawable(imageResource);

        mTitleTextView = (TextView) findViewById(R.id.title);
        mThumImage = (ImageView) findViewById(R.id.thumbnail_image);
        mContentTextView = (TextView) findViewById(R.id.content_textview);

        mTitleTextView.setText(getIntent().getStringExtra("title"));
        if (isTrue) {
            Picasso.with(this)
                    .load(getIntent().getStringExtra("image"))
                    .placeholder(R.drawable.stub)
                    .error(R.drawable.stub)
                    .into(mThumImage);
        } else {
            mThumImage.setImageDrawable(res);
        }
        //mWebView = (WebView) findViewById(R.id.webview);
        mProgressBar = (ProgressBar) findViewById(R.id.loading);
        mLink = new String();
        mContent = new String();

        mLink = getIntent().getStringExtra("link");

        //mWebView.getSettings().setJavaScriptEnabled(false);
        if (isNetworkAvailable()) {
            mAsysncTask = new loadData();
            mAsysncTask.execute(mLink);
        } else if (isCached(mLink)){
            loadFromCache(mLink);
        } else {
            mProgressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Network is unavailable", Toast.LENGTH_LONG).show();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                supportFinishAfterTransition();
                if (null != mAsysncTask){
                    mAsysncTask.cancel(true);
                    mAsysncTask = null;
                }
//                if(NavUtils.getParentActivityName(this) != null) {
//                    NavUtils.navigateUpFromSameTask(this);
//                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(conMgr.getActiveNetworkInfo() == null
                || !conMgr.getActiveNetworkInfo().isConnected()
                || !conMgr.getActiveNetworkInfo().isAvailable()) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isCached(String link) {
        Cursor resultCursor = getContentResolver().query(
                NewsContract.DetailEntry.CONTENT_URI,
                null,
                NewsContract.DetailEntry.COLUMN_LINK + " = ?",
                new String[]{link},
                NewsContract.DetailEntry.COLUMN_LINK + " LIMIT 1");

        if (resultCursor.getCount() <= 0) {
            resultCursor.close();
            return false;
        } else {
            resultCursor.close();
            return true;
        }
    }

    private void loadFromCache(String link) {
        Cursor resultCursor = getContentResolver().query(
                NewsContract.DetailEntry.CONTENT_URI,
                DETAIL_COLUMNS,
                NewsContract.DetailEntry.COLUMN_LINK + " = ?",
                new String[]{link},
                null);

        resultCursor.moveToFirst();

        mContent = resultCursor.getString(COL_DETAIL_CONTENT);
        mContentTextView.setText(mContent);
        mProgressBar.setVisibility(View.GONE);
        resultCursor.close();
    }

    private void saveToCache(String link, String content) {
        ContentValues value = new ContentValues();
        value.put(NewsContract.DetailEntry.COLUMN_LINK, link);
        value.put(NewsContract.DetailEntry.COLUMN_CONTENT, content);
        getContentResolver().insert(NewsContract.DetailEntry.CONTENT_URI, value);
    }

    private class loadData extends AsyncTask<String, Void, String> {
        String html = new String();
        Document doc = null;

        public String getContentFromUrl(String linkUrl) {
            String result = "nothing";
            try{
                Document doc = Jsoup.connect(linkUrl).get();
                Elements divContent = doc.select("div.text-conent");

                result = divContent.text();

                Log.d("detail", result);
                return result;
            } catch(Exception e) {

            }// end try block

            return null;
        }

        @Override
        protected String doInBackground(String... params) {
            return getContentFromUrl(params[0]);
        }

        @Override
        protected void onPostExecute(String s) {
            //mWebView.loadDataWithBaseURL(null, s, "text/html", "utf-8", null);
            mProgressBar.setVisibility(View.GONE);
            mContentTextView.setText(s);
            mContent = s;

            if (!isCached(mLink)) {
                saveToCache(mLink, mContent);
            }
        }
    }
}
