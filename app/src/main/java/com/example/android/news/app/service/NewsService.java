package com.example.android.news.app.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.example.android.news.app.AllNewsFragment;
import com.example.android.news.app.FootballNewsFragment;
import com.example.android.news.app.MainActivity;
import com.example.android.news.app.R;
import com.example.android.news.app.SecurityNewsFragment;
import com.example.android.news.app.SettingsActivity;
import com.example.android.news.app.database.NewsContract;
import com.example.android.news.app.database.NewsProvider;
import com.example.android.news.app.instance.RSSItem;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Created by lucky_luke on 5/16/2016.
 */
public class NewsService extends IntentService{
    private static final int NEWS_NOTIFICATION_ID = 3004;

    public NewsService() {
        super("News");
    }

    public void parseXml(String linkUrl, Uri mUri) {

        URL url = null;
        try {
            url = new URL(linkUrl);
        } catch(MalformedURLException e) {
            e.printStackTrace();
        }

        try{
            // Create required instances
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setCoalescing(true);//convert any CDATA node to text node
            DocumentBuilder db = dbf.newDocumentBuilder();

            // Parse the xml
            Document doc = db.parse(new InputSource(url.openStream()));
            doc.getDocumentElement().normalize();

            // Get all tags
            NodeList nl = doc.getElementsByTagName("item");
            int length = nl.getLength();
            Vector<ContentValues> cVVector = new Vector<ContentValues>(length);

            for(int i=0; i<length; i++) {
                // each article of news we need to get title, description, image, date
                Node currentNode = nl.item(i);
                RSSItem _item = new RSSItem();

                NodeList nChild = currentNode.getChildNodes();
                int clength = nChild.getLength();
                // Get the required elements from each item
                ContentValues newsValues = new ContentValues();

                for(int j=1; j<clength; j+=2) {
                    Node thisNode = nChild.item(j);
                    String nodeName = thisNode.getNodeName();
                    String value = nChild.item(j).getFirstChild().getNodeValue();
                    if(value != null) {
                        if("title".equals(nodeName)) {
                            _item.setTitle(value);
                        } else if("description".equals(nodeName)) {
                            // Parse the html description to get the image url
                            String html = value;
                            org.jsoup.nodes.Document docHtml = Jsoup.parse(html);
                            Elements imgEle = docHtml.select("img");
                            _item.setImage(imgEle.attr("src"));

                            // Set Description from CDATA
                            _item.setDescription(docHtml.text());
                        } else if("pubDate".equals(nodeName)) {
                            _item.setDate(value);
                        } else if("link".equals(nodeName)) {
                            _item.setLink(value);
                        }
                    }
                }// end for loop 2

                switch (NewsProvider.sUriMatcher.match(mUri)) {
                    case NewsProvider.ALLNEWS:
                        newsValues.put(NewsContract.AllNewsEntry.COLUMN_TITLE, _item.getTitle());
                        newsValues.put(NewsContract.AllNewsEntry.COLUMN_DESCRIPTION, _item.getDescription());
                        newsValues.put(NewsContract.AllNewsEntry.COLUMN_DATE, _item.getDate());
                        newsValues.put(NewsContract.AllNewsEntry.COLUMN_IMAGE, _item.getImage());
                        newsValues.put(NewsContract.AllNewsEntry.COLUMN_LINK, _item.getLink());
                        break;
                    case NewsProvider.FOOTBALLNEWS:
                        newsValues.put(NewsContract.FootballNewsEntry.COLUMN_TITLE, _item.getTitle());
                        newsValues.put(NewsContract.FootballNewsEntry.COLUMN_DESCRIPTION, _item.getDescription());
                        newsValues.put(NewsContract.FootballNewsEntry.COLUMN_DATE, _item.getDate());
                        newsValues.put(NewsContract.FootballNewsEntry.COLUMN_IMAGE, _item.getImage());
                        newsValues.put(NewsContract.FootballNewsEntry.COLUMN_LINK, _item.getLink());
                        break;
                    case NewsProvider.SECURITYNEWS:
                        newsValues.put(NewsContract.SecurityNewsEntry.COLUMN_TITLE, _item.getTitle());
                        newsValues.put(NewsContract.SecurityNewsEntry.COLUMN_DESCRIPTION, _item.getDescription());
                        newsValues.put(NewsContract.SecurityNewsEntry.COLUMN_DATE, _item.getDate());
                        newsValues.put(NewsContract.SecurityNewsEntry.COLUMN_IMAGE, _item.getImage());
                        newsValues.put(NewsContract.SecurityNewsEntry.COLUMN_LINK, _item.getLink());
                        break;
                    default:
                        throw new UnsupportedOperationException("Unknown uri: " + mUri);
                }

                cVVector.add(newsValues);
            }// end for loop 1

            int inserted = 0;

            // delete old data
            getContentResolver().delete(mUri, null, null);

            // add to database
            if ( cVVector.size() > 0 ) {
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                getContentResolver().bulkInsert(mUri, cvArray);
            }
        } catch(Exception e) {

        }// end try block
    }// end parseXml method

    @Override
    protected void onHandleIntent(Intent intent) {
        if (isNetworkAvailable()) {
            String link = intent.getStringExtra("link");
            Uri uri = Uri.parse(intent.getStringExtra("uri"));
            parseXml(link, uri);

            Intent localIntent = new Intent();
            switch (NewsProvider.sUriMatcher.match(uri)) {
                case NewsProvider.ALLNEWS:
                    localIntent.setAction(AllNewsFragment.FILTER_RECEIVER);
                    break;
                case NewsProvider.FOOTBALLNEWS:
                    localIntent.setAction(FootballNewsFragment.FILTER_RECEIVER);
                    break;
                case NewsProvider.SECURITYNEWS:
                    localIntent.setAction(SecurityNewsFragment.FILTER_RECEIVER);
                    break;
            }
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            notifyNews();
        } else {
            Toast.makeText(getBaseContext(), "Network is unavailable", Toast.LENGTH_LONG).show();
        }
    }

    private void notifyNews() {
        SharedPreferences pref = getBaseContext().getSharedPreferences(SettingsActivity.NewsPref, Context.MODE_PRIVATE);
        boolean isNotify = pref.getBoolean(SettingsActivity.NOTIFICATION, false);

        if (isNotify) {
            Intent resultIntent = new Intent(getBaseContext(), MainActivity.class);
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            getBaseContext(),
                            0,
                            resultIntent,
                            PendingIntent.FLAG_CANCEL_CURRENT
                    );

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(getBaseContext())
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("News")
                            .setContentText("Received new News")
                            .setAutoCancel(true)
                            .setContentIntent(resultPendingIntent);

            NotificationManager mNotificationManager =
                    (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(NEWS_NOTIFICATION_ID, mBuilder.build());
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

    static public class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent localIntent = new Intent(context, NewsService.class);
            localIntent.putExtra("link", intent.getStringExtra("link"));
            localIntent.putExtra("uri", intent.getStringExtra("uri"));
            context.startService(localIntent);
        }
    }
}
