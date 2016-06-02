package com.example.android.news.app.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.news.app.DetailActivity;
import com.example.android.news.app.R;
import com.example.android.news.app.instance.RSSItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by lucky_luke on 4/8/2016.
 */
public class CustomListAdapter extends BaseAdapter {
    private static LayoutInflater mLayoutInflater = null;
    private Context mContext;
    private ArrayList<RSSItem> mListData = new ArrayList<>();

    public CustomListAdapter(Context context, ArrayList<RSSItem> listData){
        mContext = context;
        mListData = listData;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mListData.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private class Holder {
        ImageView thumbnail_image;
        TextView title_textview;
        TextView description_textview;
        TextView date_textview;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Holder holder = new Holder();
        View rowView = mLayoutInflater.inflate(R.layout.list_item, parent, false);

        holder.thumbnail_image = (ImageView) rowView.findViewById(R.id.thumbnail_image);
        holder.title_textview = (TextView) rowView.findViewById(R.id.title_textview);
        holder.description_textview = (TextView) rowView.findViewById(R.id.description_textview);
        holder.date_textview = (TextView) rowView.findViewById(R.id.date_textview);

        RSSItem item = mListData.get(position);
        holder.title_textview.setText(item.getTitle());
        holder.description_textview.setText(item.getDescription());
        holder.date_textview.setText(item.getDate());
        Picasso.with(this.mContext)
                .load(item.getImage())
                .placeholder(R.drawable.stub)
                .error(R.drawable.stub)
                .into(holder.thumbnail_image);

        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(mContext, DetailActivity.class);
                i.putExtra("link", mListData.get(position).getLink());
                i.putExtra("title", mListData.get(position).getTitle());
                i.putExtra("image", mListData.get(position).getImage());
                i.putExtra("description", mListData.get(position).getDescription());
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    Pair<View, String> p1 = Pair.create((View)holder.thumbnail_image, "thumnail");
                    Pair<View, String> p2 = Pair.create((View)holder.title_textview, "title");
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, p1, p2);
                    //ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, (View) holder.thumbnail_image, "thumnail");
                    mContext.startActivity(i, options.toBundle());
                } else {
                    mContext.startActivity(i);
                }
            }
        });

        return rowView;
    }
}
