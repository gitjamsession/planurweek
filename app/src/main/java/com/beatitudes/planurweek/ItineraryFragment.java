package com.beatitudes.planurweek;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.beatitudes.planurweek.data.ScheduleContract;

import java.util.Date;

/**
 * Created by user on 25-05-2015.
 */
public class ItineraryFragment extends Fragment implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor>{


    ////private SimpleCursorAdapter mItineraryAdapter;
    private ItineraryAdapter mItineraryAdapter;

    private String mLocation;
    public static final int ITINERARY_LOADER = 0;

    ////private ArrayAdapter<String> mItineraryAdapter;

    private static final String[] ITINERARY_COLUMNS = {

            ScheduleContract.ScheduleEntry.TABLE_NAME + "." + ScheduleContract.ScheduleEntry._ID,
            ScheduleContract.ScheduleEntry.COLUMN_DATETEXT,
            ScheduleContract.ScheduleEntry.COLUMN_EVENT_NAME,
            ScheduleContract.ScheduleEntry.COLUMN_GROUP_NAME,
            ScheduleContract.ScheduleEntry.COLUMN_EVENT_URL,
            ScheduleContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    public static final int COL_SCHEDULE_ID = 0;
    public static final int COL_SCHEDULE_DATE = 1;
    public static final int COL_SCHEDULE_EVENT_NAME = 2;
    public static final int COL_SCHEDULE_GRP_NAME = 3;
    public static final int COL_SCHEDULE_EVENT_URL_NAME = 4;
    public static final int COL_LOCATION_SETTING = 5;


    public ItineraryFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.itineraryfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id==R.id.action_refresh){
            updateSchedule();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //ArrayAdapter mItineraryAdapter;
        /***mItineraryAdapter = new ArrayAdapter<String>(

                //CONTEXT
                getActivity(),
                //List Item Layout ID
                R.layout.list_item_itinerary,
                //List Item textview ID
                R.id.list_item_itinerary_textview,

                //Itinerary Data
                new ArrayList<String>()
        );
        ***/
        /***mItineraryAdapter = new SimpleCursorAdapter(
                getActivity(),
                R.layout.list_item_itinerary,
                null,
                // the column names to use to fill the textviews
                new String[]{ScheduleContract.ScheduleEntry.COLUMN_DATETEXT,
                        ScheduleContract.ScheduleEntry.COLUMN_EVENT_NAME,
//                        ScheduleContract.ScheduleEntry.COLUMN_GROUP_NAME,
//                        ScheduleContract.ScheduleEntry.COLUMN_EVENT_URL,


                },
                // the textviews to fill with the data pulled from the columns above
                new int[]{
                          R.id.list_item_date_textview,
                        R.id.list_item_itinerary_textview,
//                        R.id.list_item_grpname_textview,
//                        R.id.list_item_eventurlname_textview,
//                        R.id.list_item_low_textview
                },
                0
        );
***/
        mItineraryAdapter = new ItineraryAdapter(getActivity(), null, 0);

/***        mItineraryAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

                switch (columnIndex) {

                    case COL_SCHEDULE_DATE: {
                        String dateString = cursor.getString(columnIndex);
                        TextView dateView = (TextView) view;
                        dateView.setText(Utility.formatDate(dateString));
                        return true;
                    }
                }
                return false;
            }
        });

***/
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

       /***String[] itineraryArray = {
                "Today - Holy Mass - 6PM to 7PM",
                "Monday - BLUES - 12PM to 7PM",
                "Tuesday - NOVENA - 6PM to 7PM",
                "Wednesday - Praise & Worship - 1PM to 2PM",
                "Thursday - St.Francis Novena - 6PM to 7PM",
                "Friday - Release Testing - 9.30AM to 9PM",
                "Saturday - PARTY!!! - 10AM to 12PM"
        };***/
        ////List<String> weekItinerary = new ArrayList<String>(Arrays.asList(itineraryArray));


/***
        ListView listView = (ListView) rootView.findViewById(R.id.listView_itinerary);
        // Binding Adapter to ListView
        listView.setAdapter(mItineraryAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
            {
//                String itinerary = mItineraryAdapter.getItem(position);
                //Use Intent instead of Toast
                // Toast.makeText(getActivity(),itinerary,Toast.LENGTH_SHORT).show();
//                Intent intent = new Intent(getActivity(),DetailActivity.class).putExtra(Intent.EXTRA_TEXT,itinerary);
//                startActivity(intent);
                Cursor cursor = mItineraryAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    Intent intent = new Intent(getActivity(), DetailActivity.class)
                            .putExtra(DetailActivity.DetailFragment.DATE_KEY, cursor.getString(COL_SCHEDULE_DATE));
//                    Intent intent = new Intent(getActivity(),DetailActivity.class).putExtra(Intent.EXTRA_TEXT,cursor.getString());
                    startActivity(intent);
                }

            }
        });

***/
        ListView listView = (ListView) rootView.findViewById(R.id.listView_itinerary);
        listView.setAdapter(mItineraryAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = mItineraryAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    Intent intent = new Intent(getActivity(), DetailActivity.class)
                            .putExtra(DetailActivity.DetailFragment.DATE_KEY, cursor.getString(COL_SCHEDULE_DATE));
                    startActivity(intent);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(ITINERARY_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }



    private void updateSchedule(){

//        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getActivity());
        ////FetchScheduleTask scheduleTask = new FetchScheduleTask();

//        String location = prefs.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));
//        String countrycode = prefs.getString(getString(R.string.pref_countrycode_key),getString(R.string.pref_countrycode_default));
//        new FetchScheduleTask(getActivity(),mItineraryAdapter).execute(countrycode,location);
        String location = Utility.getPreferredLocation(getActivity());
        String countrycode = Utility.getPreferredCountryCode(getActivity());
        new FetchScheduleTask(getActivity()).execute(countrycode,location);

    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLocation != null && !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
            getLoaderManager().restartLoader(ITINERARY_LOADER, null, this);
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        updateSchedule();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String startDate = ScheduleContract.getDbDateString(new Date());

        // Sort order:  Ascending, by date.
        String sortOrder = ScheduleContract.ScheduleEntry.COLUMN_DATETEXT + " ASC";

        mLocation = Utility.getPreferredLocation(getActivity());
        Uri scheduleForLocationUri = ScheduleContract.ScheduleEntry.buildScheduleLocationWithStartDate(
                mLocation, startDate);

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                scheduleForLocationUri,
                ITINERARY_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mItineraryAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mItineraryAdapter.swapCursor(null);
    }

}
