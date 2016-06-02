package com.example.android.news.app;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.news.app.adapter.NewsAdapter;
import com.example.android.news.app.database.NewsContract;
import com.example.android.news.app.instance.RSSItem;
import com.example.android.news.app.service.NewsService;
import com.example.android.news.app.sync.DownloadDataAsyncTask;

import java.util.ArrayList;

/**
 * Created by lucky_luke on 4/6/2016.
 */
public class SecurityNewsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String SECURITY_LINK = "http://www.24h.com.vn/upload/rss/anninhhinhsu.rss";

    private static final int SecurityNewsLoader = 2;

    public static final String FILTER_RECEIVER = "com.example.android.news.app.SecurityNewsFragment.SecurityNewsReceiver";

    private static int INTERVAL = 17 * 60 * 1000;

    private static ArrayList<RSSItem> mRSSItems = null;
    private DownloadDataAsyncTask mDownloadDataAsyncTask;

    private ListView mListView;
    private NewsAdapter mSecurityNewsAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private static final String[] SECURITYNEWS_COLUMNS = {
            NewsContract.SecurityNewsEntry.TABLE_NAME + "." + NewsContract.SecurityNewsEntry._ID,
            NewsContract.SecurityNewsEntry.COLUMN_TITLE,
            NewsContract.SecurityNewsEntry.COLUMN_DESCRIPTION,
            NewsContract.SecurityNewsEntry.COLUMN_DATE,
            NewsContract.SecurityNewsEntry.COLUMN_IMAGE,
            NewsContract.SecurityNewsEntry.COLUMN_LINK
    };

    public static final int COL_SECURITYNEWS_ID = 0;
    public static final int COL_SECURITYNEWS_TITLE = 1;
    public static final int COL_SECURITYNEWS_DESCRIPTION = 2;
    public static final int COL_SECURITYNEWS_DATE = 3;
    public static final int COL_SECURITYNEWS_IMAGE = 4;
    public static final int COL_SECURITYNEWS_LINK = 5;

    private AlarmManager mAlarmManager;
    private PendingIntent pi;
    private SecurityNewsResponseReceiver mResponseReceiver;

    public SecurityNewsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupResponseReceiver();
        update();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_security_news, container, false);

        mSecurityNewsAdapter = new NewsAdapter(getActivity(), null, 0, NewsContract.SecurityNewsEntry.CONTENT_URI);

        mListView = (ListView) view.findViewById(R.id.security_news_listview);
        mListView.setAdapter(mSecurityNewsAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (null != cursor) {
                    String title = cursor.getString(COL_SECURITYNEWS_TITLE);
                    String description = cursor.getString(COL_SECURITYNEWS_DESCRIPTION);
                    String date = cursor.getString(COL_SECURITYNEWS_DATE);
                    String image = cursor.getString(COL_SECURITYNEWS_IMAGE);
                    String link = cursor.getString(COL_SECURITYNEWS_LINK);
                    Intent i = new Intent(getActivity(), DetailActivity.class);
                    i.putExtra(SECURITYNEWS_COLUMNS[COL_SECURITYNEWS_LINK], link);
                    i.putExtra(SECURITYNEWS_COLUMNS[COL_SECURITYNEWS_TITLE], title);
                    i.putExtra(SECURITYNEWS_COLUMNS[COL_SECURITYNEWS_IMAGE], image);
                    i.putExtra(SECURITYNEWS_COLUMNS[COL_SECURITYNEWS_DESCRIPTION], description);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        Pair<View, String> p1 = Pair.create(view.findViewById(R.id.thumbnail_image), "thumnail");
                        Pair<View, String> p2 = Pair.create(view.findViewById(R.id.title_textview), "title");
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) getActivity(), p1, p2);
                        //ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, (View) holder.thumbnail_image, "thumnail");
                        getActivity().startActivity(i, options.toBundle());
                    } else {
                        getActivity().startActivity(i);
                    }
                }
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swiperefresh);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                cancelAlarm();
                update();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(SecurityNewsLoader, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    private void update() {
        //mDownloadDataAsyncTask = new DownloadDataAsyncTask(getContext(), NewsContract.SecurityNewsEntry.CONTENT_URI);
        //mDownloadDataAsyncTask.execute(SECURITY_LINK);
        Intent alarmIntent = new Intent(getActivity(), NewsService.AlarmReceiver.class);
        alarmIntent.putExtra("link", SECURITY_LINK);
        alarmIntent.putExtra("uri", NewsContract.SecurityNewsEntry.CONTENT_URI.toString());

        pi = PendingIntent.getBroadcast(getActivity(), 0, alarmIntent, 0);

        mAlarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), INTERVAL, pi);

        Intent intent = new Intent(getActivity(), NewsService.class);
        intent.putExtra("link", SECURITY_LINK);
        intent.putExtra("uri", NewsContract.SecurityNewsEntry.CONTENT_URI.toString());
        getActivity().startService(intent);
    }

    private void refresh() {
        getLoaderManager().restartLoader(SecurityNewsLoader, null, this);
    }

    private void cancelAlarm() {
        if (null != mAlarmManager) {
            mAlarmManager.cancel(pi);
        }
    }

    private void setupResponseReceiver() {
        IntentFilter intentFilter = new IntentFilter(FILTER_RECEIVER);
        mResponseReceiver = new SecurityNewsResponseReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mResponseReceiver, intentFilter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case SecurityNewsLoader:
                return new CursorLoader(getActivity(),
                        NewsContract.SecurityNewsEntry.CONTENT_URI,
                        SECURITYNEWS_COLUMNS,
                        null,
                        null,
                        null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mSecurityNewsAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSecurityNewsAdapter.swapCursor(null);
    }

    private class SecurityNewsResponseReceiver extends BroadcastReceiver {
        public SecurityNewsResponseReceiver() {}

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(FILTER_RECEIVER)) {
                refresh();
            }
        }
    }
}
