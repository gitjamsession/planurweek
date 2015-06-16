package com.beatitudes.planurweek;


import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.Log;

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
import java.util.Date;
import java.util.Vector;

/**
 * Created by user on 14-06-2015.
 */
public class FetchScheduleTask extends AsyncTask<String, Void, Void> {

    private final String LOG_TAG = FetchScheduleTask.class.getSimpleName();

//    private ArrayAdapter<String> mScheduleAdapter;
//    private SimpleCursorAdapter mScheduleAdapter;
    private final Context mContext;

//    public FetchScheduleTask(Context context, ArrayAdapter<String> scheduleAdapter) {
    public FetchScheduleTask(Context context){ ////}, SimpleCursorAdapter scheduleAdapter) {
        mContext = context;
//        mScheduleAdapter = scheduleAdapter;
    }

    private long addLocation(String locationSetting, String countryCode){ ////, double lat, double lon) {

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
            ////locationValues.put(ScheduleContract.LocationEntry.COLUMN_CITY_NAME, cityName);
            locationValues.put(ScheduleContract.LocationEntry.COLUMN_COUNTRY_CODE, countryCode);
            ////locationValues.put(LocationEntry.COLUMN_COORD_LAT, lat);
            ////locationValues.put(LocationEntry.COLUMN_COORD_LONG, lon);


            Uri locationInsertUri = mContext.getContentResolver()
                    .insert(ScheduleContract.LocationEntry.CONTENT_URI, locationValues);

            return ContentUris.parseId(locationInsertUri);
        }
    }

    private void getScheduleDataFromJson(String itineraryJsonStr, int numDays, String countryCode, String locationSetting)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String MU_RESULTS = "results";
//        final String MU_VENUE = "venue";
//        final String MU_VENUECITY = "city";
//        final String MU_VENUENAME = "name";
//        final String MU_VENUEADDRESS1 = "address_1";

        final String MU_GROUP = "group";
        final String MU_GRPNAME = "name";
        final String MU_GRPURLNAME = "urlname";

        final String MU_EVENTURL = "event_url";
        final String MU_EVENTNAME = "name";
        final String MU_EVENTTIME = "time";


        final String MU_META = "meta";
        final String MU_METATOTALCOUNT = "total_count";

        JSONObject itineraryJson = new JSONObject(itineraryJsonStr);
        JSONArray scheduleArray = itineraryJson.getJSONArray(MU_RESULTS);

//        JSONObject event_url_Json = itineraryJson.getJSONObject(MU_EVENTURL);
//        String event_url = event_url_Json.getString(MU_EVENTURL);
//
//        Log.v(LOG_TAG, event_url + ", event url : " + event_url);

        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        // Insert the location into the database.
        long locationID = addLocation(locationSetting, countryCode);

        String[] resultStrs = new String[scheduleArray.length()];

        Vector<ContentValues> cVVector = new Vector<ContentValues>(scheduleArray.length());

        for(int i = 0; i < scheduleArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String groupname,groupurlname;
            String eventname,eventurl,eventtime;
//            String venuename,venueaddress,venuecity;

            long dateTime;

            // Get the JSON object representing the day
            JSONObject daySchedule = scheduleArray.getJSONObject(i);

            dateTime = dayTime.setJulianDay(julianStartDay+i);
            day = getReadableDateString(dateTime);

//            JSONObject venueObject = daySchedule.getJSONObject(MU_VENUE);
//            venuename = venueObject.getString(MU_VENUENAME);
//            venueaddress = venueObject.getString(MU_VENUEADDRESS1);
//            venuecity = venueObject.getString(MU_VENUECITY);

            eventurl = daySchedule.getString(MU_EVENTURL);
            eventname = daySchedule.getString(MU_EVENTNAME);
            eventtime = daySchedule.getString(MU_EVENTTIME);

            JSONObject groupObject = daySchedule.getJSONObject(MU_GROUP);
            groupname = groupObject.getString(MU_GRPNAME);
            groupurlname = groupObject.getString(MU_GRPURLNAME);


            ContentValues scheduleValues = new ContentValues();
            scheduleValues.put(ScheduleContract.ScheduleEntry.COLUMN_LOC_KEY, locationID);
            scheduleValues.put(ScheduleContract.ScheduleEntry.COLUMN_DATETEXT,
                    ScheduleContract.getDbDateString(new Date(dateTime)));
            scheduleValues.put(ScheduleContract.ScheduleEntry.COLUMN_EVENT_TIME, eventtime);
//            scheduleValues.put(ScheduleContract.ScheduleEntry.COLUMN_VENUE_NAME, venuename);
//            scheduleValues.put(ScheduleContract.ScheduleEntry.COLUMN_VENUE_ADDRESS1, venueaddress);
//            scheduleValues.put(ScheduleContract.ScheduleEntry.COLUMN_VENUE_CITY, venuecity);
            scheduleValues.put(ScheduleContract.ScheduleEntry.COLUMN_EVENT_URL, eventurl);
            scheduleValues.put(ScheduleContract.ScheduleEntry.COLUMN_GROUP_NAME, groupname);
            scheduleValues.put(ScheduleContract.ScheduleEntry.COLUMN_EVENT_NAME, eventname);


            cVVector.add(scheduleValues);


            //highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + "-" + eventurl + "-" + groupname + "-" + groupurlname;

        }


        for (String s : resultStrs) {
            Log.v("RESULT_STRING", "Schedule entry: " + s);
        }
//        return resultStrs;

        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            mContext.getContentResolver().bulkInsert(ScheduleContract.ScheduleEntry.CONTENT_URI, cvArray);
        }
    }


    @Override
    protected Void doInBackground(String... params) {

        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String itineraryJsonStr = null;

        if (params.length == 0) {
            return null;
        }

        String cityName = params[0];
        String countryCode = params[1];

        String status = "upcoming";

        String time = "&time=,1w";

        String key = "463f4e2e2d37672b55636f9636921";

        int numDays = 7;

        try {


            final String ITINERARY_BASE_URL = "https://api.meetup.com/2/open_events.json?time=,1w";
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
            getScheduleDataFromJson(itineraryJsonStr,numDays,cityName,countryCode);
        }catch (JSONException e){
            Log.e(LOG_TAG,e.getMessage(),e);
            e.printStackTrace();
        }
        return null;
    }



    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }
}
