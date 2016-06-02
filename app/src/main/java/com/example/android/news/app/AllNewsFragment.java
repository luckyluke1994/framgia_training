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
import android.widget.Toast;

import com.example.android.news.app.adapter.NewsAdapter;
import com.example.android.news.app.database.NewsContract;
import com.example.android.news.app.instance.RSSItem;
import com.example.android.news.app.service.NewsService;
import com.example.android.news.app.sync.DownloadDataAsyncTask;

import java.util.ArrayList;

/**
 * Created by lucky_luke on 4/6/2016.
 */
public class AllNewsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String ALL_NEWS_LINK = "http://www.24h.com.vn/upload/rss/tintuctrongngay.rss";

    private static final int AllNewsLoader = 0;

    public static final String FILTER_RECEIVER = "com.example.android.news.app.AllNewsFragment.AllNewsReceiver";

    private static int INTERVAL = 15 * 60 * 1000;

    private static ArrayList<RSSItem> mRSSItems = null;
    private DownloadDataAsyncTask mDownloadDataAsyncTask;

    private ListView mListView;
    private NewsAdapter mAllNewsAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private static final String[] ALLNEWS_COLUMNS = {
            NewsContract.AllNewsEntry.TABLE_NAME + "." + NewsContract.AllNewsEntry._ID,
            NewsContract.AllNewsEntry.COLUMN_TITLE,
            NewsContract.AllNewsEntry.COLUMN_DESCRIPTION,
            NewsContract.AllNewsEntry.COLUMN_DATE,
            NewsContract.AllNewsEntry.COLUMN_IMAGE,
            NewsContract.AllNewsEntry.COLUMN_LINK
    };

    public static final int COL_ALLNEWS_ID = 0;
    public static final int COL_ALLNEWS_TITLE = 1;
    public static final int COL_ALLNEWS_DESCRIPTION = 2;
    public static final int COL_ALLNEWS_DATE = 3;
    public static final int COL_ALLNEWS_IMAGE = 4;
    public static final int COL_ALLNEWS_LINK = 5;

    private AlarmManager mAlarmManager;
    private PendingIntent pi;
    private AllNewsReponseReceiver mResponseReceiver;

    public AllNewsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupResponseReceiver();
        update();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_all_news, container, false);

        mAllNewsAdapter = new NewsAdapter(getActivity(), null, 0, NewsContract.AllNewsEntry.CONTENT_URI);

        mListView = (ListView) view.findViewById(R.id.all_news_listview);
        mListView.setAdapter(mAllNewsAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (null != cursor) {
                    String title = cursor.getString(COL_ALLNEWS_TITLE);
                    String description = cursor.getString(COL_ALLNEWS_DESCRIPTION);
                    String date = cursor.getString(COL_ALLNEWS_DATE);
                    String image = cursor.getString(COL_ALLNEWS_IMAGE);
                    String link = cursor.getString(COL_ALLNEWS_LINK);
                    Intent i = new Intent(getActivity(), DetailActivity.class);
                    i.putExtra(ALLNEWS_COLUMNS[COL_ALLNEWS_LINK], link);
                    i.putExtra(ALLNEWS_COLUMNS[COL_ALLNEWS_TITLE], title);
                    i.putExtra(ALLNEWS_COLUMNS[COL_ALLNEWS_IMAGE], image);
                    i.putExtra(ALLNEWS_COLUMNS[COL_ALLNEWS_DESCRIPTION], description);
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
        getLoaderManager().initLoader(AllNewsLoader, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void update() {
        //mDownloadDataAsyncTask = new DownloadDataAsyncTask(getContext(), NewsContract.AllNewsEntry.CONTENT_URI);
        //mDownloadDataAsyncTask.execute(ALL_NEWS_LINK);
        Intent alarmIntent = new Intent(getActivity(), NewsService.AlarmReceiver.class);
        alarmIntent.putExtra("link", ALL_NEWS_LINK);
        alarmIntent.putExtra("uri", NewsContract.AllNewsEntry.CONTENT_URI.toString());

        pi = PendingIntent.getBroadcast(getActivity(), 0, alarmIntent, 0);

        mAlarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), INTERVAL, pi);

        Intent intent = new Intent(getActivity(), NewsService.class);
        intent.putExtra("link", ALL_NEWS_LINK);
        intent.putExtra("uri", NewsContract.AllNewsEntry.CONTENT_URI.toString());
        getActivity().startService(intent);
        // make new loader when data changed to use new cursor data instead old cursor data
        //getLoaderManager().restartLoader(AllNewsLoader, null, this);
    }

    private void refresh() {
        getLoaderManager().restartLoader(AllNewsLoader, null, this);
        Toast.makeText(getActivity(), "refresh()", Toast.LENGTH_LONG).show();
    }

    private void cancelAlarm() {
        if (null != mAlarmManager) {
            mAlarmManager.cancel(pi);
            mAlarmManager = null;
        }
    }

    private void setupResponseReceiver() {
        IntentFilter intentFilter = new IntentFilter(FILTER_RECEIVER);
        mResponseReceiver = new AllNewsReponseReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mResponseReceiver, intentFilter);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id) {
            case AllNewsLoader:
                return new CursorLoader(getActivity(),
                        NewsContract.AllNewsEntry.CONTENT_URI,
                        ALLNEWS_COLUMNS,
                        null,
                        null,
                        null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAllNewsAdapter.swapCursor(data);
        Toast.makeText(getActivity(), "ddd"+loader.getId(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAllNewsAdapter.swapCursor(null);
    }

    private class AllNewsReponseReceiver extends BroadcastReceiver {
        public AllNewsReponseReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(FILTER_RECEIVER)) {
                refresh();
            }
        }
    }
}
