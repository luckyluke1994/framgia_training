package com.example.android.news.app.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.news.app.AllNewsFragment;
import com.example.android.news.app.FootballNewsFragment;
import com.example.android.news.app.R;
import com.example.android.news.app.SecurityNewsFragment;
import com.example.android.news.app.SettingsActivity;
import com.example.android.news.app.database.NewsProvider;
import com.squareup.picasso.Picasso;

/**
 * Created by lucky_luke on 5/13/2016.
 */
public class NewsAdapter extends CursorAdapter {
    Uri mUri;

    public NewsAdapter(Context context, Cursor c, int flags, Uri uri) {
        super(context, c, flags);
        mUri = uri;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        final ViewHolder viewHolder = (ViewHolder) view.getTag();
        String title, description, date, image;
        SharedPreferences pref = context.getSharedPreferences(SettingsActivity.NewsPref, Context.MODE_PRIVATE);
        boolean isTrue = pref.getBoolean(SettingsActivity.MODE_NETWORK, true);

        String uri = "@drawable/stub";
        int imageResource = context.getResources().getIdentifier(uri, null, context.getPackageName());
        Drawable res = context.getResources().getDrawable(imageResource);

        switch (NewsProvider.sUriMatcher.match(mUri)) {
            case NewsProvider.ALLNEWS:
                title = cursor.getString(AllNewsFragment.COL_ALLNEWS_TITLE);
                viewHolder.titleView.setText(title);
                description = cursor.getString(AllNewsFragment.COL_ALLNEWS_DESCRIPTION);
                viewHolder.descriptionView.setText(description);
                date = cursor.getString(AllNewsFragment.COL_ALLNEWS_DATE);
                viewHolder.dateView.setText(date);
                image = cursor.getString(AllNewsFragment.COL_ALLNEWS_IMAGE);
                if (isTrue) {
                    Picasso.with(context)
                            .load(image)
                            .placeholder(R.drawable.stub)
                            .error(R.drawable.stub)
                            .into(viewHolder.iconView);
                } else {
                    viewHolder.iconView.setImageDrawable(res);
                }
                break;
            case NewsProvider.FOOTBALLNEWS:
                title = cursor.getString(FootballNewsFragment.COL_FOOTBALLNEWS_TITLE);
                viewHolder.titleView.setText(title);
                description = cursor.getString(FootballNewsFragment.COL_FOOTBALLNEWS_DESCRIPTION);
                viewHolder.descriptionView.setText(description);
                date = cursor.getString(FootballNewsFragment.COL_FOOTBALLNEWS_DATE);
                viewHolder.dateView.setText(date);
                image = cursor.getString(FootballNewsFragment.COL_FOOTBALLNEWS_IMAGE);
                if (isTrue) {
                    Picasso.with(context)
                            .load(image)
                            .placeholder(R.drawable.stub)
                            .error(R.drawable.stub)
                            .into(viewHolder.iconView);
                } else {
                    viewHolder.iconView.setImageDrawable(res);
                }
                break;
            case NewsProvider.SECURITYNEWS:
                title = cursor.getString(SecurityNewsFragment.COL_SECURITYNEWS_TITLE);
                viewHolder.titleView.setText(title);
                description = cursor.getString(SecurityNewsFragment.COL_SECURITYNEWS_DESCRIPTION);
                viewHolder.descriptionView.setText(description);
                date = cursor.getString(SecurityNewsFragment.COL_SECURITYNEWS_DATE);
                viewHolder.dateView.setText(date);
                image = cursor.getString(SecurityNewsFragment.COL_SECURITYNEWS_IMAGE);
                if (isTrue) {
                    Picasso.with(context)
                            .load(image)
                            .placeholder(R.drawable.stub)
                            .error(R.drawable.stub)
                            .into(viewHolder.iconView);
                } else {
                    viewHolder.iconView.setImageDrawable(res);
                }
                break;
        }
    }

    public static class ViewHolder {
        public ImageView iconView;
        public TextView titleView;
        public TextView descriptionView;
        public TextView dateView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.thumbnail_image);
            titleView = (TextView) view.findViewById(R.id.title_textview);
            descriptionView = (TextView) view.findViewById(R.id.description_textview);
            dateView = (TextView) view.findViewById(R.id.date_textview);
        }
    }
}
