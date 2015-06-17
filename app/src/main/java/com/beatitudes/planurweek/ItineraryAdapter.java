package com.beatitudes.planurweek;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by user on 18-06-2015.
 */
public class ItineraryAdapter extends CursorAdapter {

    private static final int VIEW_TYPE_COUNT = 2;
    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;



    public static class ViewHolder {
        public final ImageView iconView;
        public final TextView dateView;
        public final TextView itineraryView;
//        public final TextView groupnameView;
//        public final TextView eventurlView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.list_item_icon);
            dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
            itineraryView = (TextView) view.findViewById(R.id.list_item_itinerary_textview);
//            groupnameView = (TextView) view.findViewById(R.id.list_item_groupname_textview);
//            eventurlView = (TextView) view.findViewById(R.id.list_item_eventurl_textview);
        }
    }

    public ItineraryAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                layoutId = R.layout.list_item_itinerary_today;
                break;
            }
            case VIEW_TYPE_FUTURE_DAY: {
                layoutId = R.layout.list_item_itinerary;
                break;
            }
        }
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        int viewType = getItemViewType(cursor.getPosition());
        switch (viewType) {
            case VIEW_TYPE_TODAY: {
                viewHolder.iconView.setImageResource(R.mipmap.ic_launcher);
                break;
            }
            case VIEW_TYPE_FUTURE_DAY: {

                break;
            }
        }

        // Read date from cursor
        String dateString = cursor.getString(ItineraryFragment.COL_SCHEDULE_DATE);
        // Find TextView and set formatted date on it
//        viewHolder.dateView.setText(Utility.formatDate(dateString));
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context, dateString));


        String eventname = cursor.getString(ItineraryFragment.COL_SCHEDULE_EVENT_NAME);
        viewHolder.itineraryView.setText(eventname);


//        String groupname = cursor.getString(ItineraryFragment.COL_SCHEDULE_GRP_NAME);
//        viewHolder.itineraryView.setText(groupname);

//        String eventurl = cursor.getString(ItineraryFragment.COL_SCHEDULE_EVENT_URL_NAME);
//        viewHolder.itineraryView.setText(eventurl);


    }
    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }
}
