package com.beatitudes.planurweek;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.beatitudes.planurweek.data.ScheduleContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by user on 25-05-2015.
 */
public class ItineraryFragment extends Fragment {

    private ArrayAdapter<String> mItineraryAdapter;

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
        mItineraryAdapter = new ArrayAdapter<String>(
                //CONTEXT
                getActivity(),
                //List Item Layout ID
                R.layout.list_item_itinerary,
                //List Item textview ID
                R.id.list_item_itinerary_textview,

                //Itinerary Data
                new ArrayList<String>()
        );

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



        ListView listView = (ListView) rootView.findViewById(R.id.listView_itinerary);
        // Binding Adapter to ListView
        listView.setAdapter(mItineraryAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
            {
                String itinerary = mItineraryAdapter.getItem(position);
                //Use Intent instead of Toast
                // Toast.makeText(getActivity(),itinerary,Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(),DetailActivity.class).putExtra(Intent.EXTRA_TEXT,itinerary);
                startActivity(intent);
            }
        });


        return rootView;
    }

    private void updateSchedule(){

        SharedPreferences prefs= PreferenceManager.getDefaultSharedPreferences(getActivity());
        ////FetchScheduleTask scheduleTask = new FetchScheduleTask();

        String location = prefs.getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));
        String countrycode = prefs.getString(getString(R.string.pref_countrycode_key),getString(R.string.pref_countrycode_default));
        new FetchScheduleTask(getActivity()).execute(countrycode,location);
    }

    @Override
    public void onStart(){
        super.onStart();
        updateSchedule();
    }

    public class FetchScheduleTask extends AsyncTask<String, Void, String[]> {

        public final String LOG_TAG = ItineraryFragment.class.getSimpleName();
        private final Context mContext;

        /***public FetchScheduleTask(FragmentActivity activity) {
        }***/
        public FetchScheduleTask(Context context) {
            mContext = context;
        }

        private long addLocation(String locationSetting, String cityName){ ////, double lat, double lon) {

            // First, check if the location with this city name exists in the db
            Cursor cursor = mContext.getContentResolver().query(
                    ScheduleContract.LocationEntry.CONTENT_URI,
                    new String[]{ScheduleContract.LocationEntry._ID},
                    ScheduleContract.LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                    new String[]{locationSetting},
                    null);

            if (cursor.moveToFirst()) {
                int locationIdIndex = cursor.getColumnIndex(ScheduleContract.LocationEntry._ID);
                return cursor.getLong(locationIdIndex);
            } else {
                ContentValues locationValues = new ContentValues();
                locationValues.put(ScheduleContract.LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
                locationValues.put(ScheduleContract.LocationEntry.COLUMN_CITY_NAME, cityName);
                ////locationValues.put(LocationEntry.COLUMN_COORD_LAT, lat);
                ////locationValues.put(LocationEntry.COLUMN_COORD_LONG, lon);

                Uri locationInsertUri = mContext.getContentResolver()
                        .insert(ScheduleContract.LocationEntry.CONTENT_URI, locationValues);

                return ContentUris.parseId(locationInsertUri);
            }
        }


        @Override
        protected String[] doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String itineraryJsonStr = null;

            if (params.length == 0) {
                return null;
            }
            String locationQuery = params[1];
            String cityName = params[0];

            String status = "upcoming";

            String time = "&date=,1w";

            String key = "463f4e2e2d37672b55636f9636921";

            int numDays = 7;

            try {


                final String ITINERARY_BASE_URL = "https://api.meetup.com/2/open_events.json?date=,1w";
                final String COUNTRY_PARAM = "country";
                final String CITY_PARAM = "city";
                ////final String STATUS_PARAM= "status";
                final String TIME_PARAM = "time";
                final String KEY_PARAM = "key";

                Uri builtUri = Uri.parse(ITINERARY_BASE_URL).buildUpon()
                                .appendQueryParameter(COUNTRY_PARAM,params[0])
                                .appendQueryParameter(CITY_PARAM,params[1])
                                //.appendQueryParameter(STATUS_PARAM, status)
                                //.appendQueryParameter(TIME_PARAM,time)
                                .appendQueryParameter(KEY_PARAM,key)
                                .build();
                URL url = new URL(builtUri.toString());
                Log.v(LOG_TAG, "Built URI" + builtUri.toString());


                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {

                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                itineraryJsonStr = buffer.toString();
                Log.v(LOG_TAG,"Itinerary JSON String"+itineraryJsonStr);

            } catch (IOException e) {
                Log.e("ItineraryFragment", "Error ", e);

                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("ItineraryFragment", "Error closing stream", e);
                    }
                }
            }
            try {
                return getScheduleDataFromJson(itineraryJsonStr,numDays,locationQuery,cityName);
            }catch (JSONException e){
                Log.e(LOG_TAG,e.getMessage(),e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {

            //super.onPostExecute(result);
            if(result!=null){
                mItineraryAdapter.clear();
                for(String itineraryJsonStr : result)
                {
                    mItineraryAdapter.add(itineraryJsonStr);
                }
            }
        }
    }


    

    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    private String[] getScheduleDataFromJson(String itineraryJsonStr,int numDays, String locationSetting, String cityName)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String MU_RESULTS = "results";
        final String MU_VENUE = "venue";
        final String MU_VENUENAME = "name";
        final String MU_VENUEADDRESS1 = "address_1";

        final String MU_GROUP = "group";
        final String MU_GRPNAME = "name";
        final String MU_GRPURLNAME = "urlname";
        final String MU_EVENTURL = "event_url";

        final String MU_META = "meta";
        final String MU_METATOTALCOUNT = "total_count";

        JSONObject itineraryJson = new JSONObject(itineraryJsonStr);
        JSONArray scheduleArray = itineraryJson.getJSONArray(MU_RESULTS);

        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        String[] resultStrs = new String[scheduleArray.length()];
        for(int i = 0; i < scheduleArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;

            // Get the JSON object representing the day
            JSONObject daySchedule = scheduleArray.getJSONObject(i);


            long dateTime;
            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay+i);
            day = getReadableDateString(dateTime);


            JSONObject groupObject = daySchedule.getJSONObject(MU_GROUP);
            String groupname = groupObject.getString(MU_GRPNAME);
            String groupurlname = groupObject.getString(MU_GRPURLNAME);

            //highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + "-" + "-" + groupname + "-" + groupurlname;


        }

        for (String s : resultStrs) {
            Log.v("RESULT_STRING", "Schedule entry: " + s);
        }
        return resultStrs;

    }
}
