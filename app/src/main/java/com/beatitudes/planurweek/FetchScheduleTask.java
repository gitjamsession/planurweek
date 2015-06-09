package com.beatitudes.planurweek;

/**
 * Created by user on 06-06-2015.
 */

/***
public class FetchScheduleTask extends AsyncTask<String, Void, String[]> {

    private final String LOG_TAG = FetchScheduleTask.class.getSimpleName();
    private final Context mContext;

    public FetchScheduleTask(Context context) {
        mContext = context;
    }


    private long addLocation(String locationSetting, String cityName) {////, String countryCode) {

        // First, check if the location with this city name exists in the db
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                new String[]{LocationEntry._ID},
                LocationEntry.COLUMN_LOCATION_SETTING + " = ?",
                new String[]{cityName},
                null);

        if (cursor.moveToFirst()) {
            int locationIdIndex = cursor.getColumnIndex(LocationEntry._ID);
            return cursor.getLong(locationIdIndex);
        } else {
            ContentValues locationValues = new ContentValues();
            locationValues.put(LocationEntry.COLUMN_LOCATION_SETTING, locationSetting);
            locationValues.put(LocationEntry.COLUMN_CITY_NAME, cityName);
            ////locationValues.put(LocationEntry.COLUMN_COUNTRY_CODE, countryCode);
            ////locationValues.put(LocationEntry.COLUMN_COORD_LONG, lon);

            Uri locationInsertUri = mContext.getContentResolver()
                    .insert(LocationEntry.CONTENT_URI, locationValues);

            return ContentUris.parseId(locationInsertUri);
        }
    }

    private String getReadableDateString(long time) {
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }


    private String[] getScheduleDataFromJson(String itineraryJsonStr, int numDays, String locationsetting, String cityname) ////, String countrycode)
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

        ////JSONArray metaArray = itineraryJson.getJSONArray(MU_META);

        // MU returns daily forecasts based upon the local time of the city that is being
        // asked for, which means that we need to know the GMT offset to translate this data
        // properly.

        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.

        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        long locationID = addLocation(locationsetting, cityname);////,countrycode);

        String[] resultStrs = new String[scheduleArray.length()];


        for (int i = 0; i < scheduleArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;

            // Get the JSON object representing the day
            JSONObject daySchedule = scheduleArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime;
            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay + i);
            day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            ////JSONObject venueObject = daySchedule.getJSONObject(MU_VENUE).getJSONObject(0);
            ////JSONObject venueObject = daySchedule.getJSONObject(MU_VENUE);
            ////JSONArray venueArray = daySchedule.getJSONArray(MU_VENUE);
            /////String venuename = venueObject.getString(MU_VENUENAME);
            ////String venueaddress = venueObject.getString(MU_VENUEADDRESS1);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
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

    @Override
    protected String[] doInBackground(String... params) {

        if (params.length == 0) {
            return null;
        }
        String locationSetting = params[0];
        String cityname = params[1];
        ////String countrycode = "IN"; ////params[2];


        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String itineraryJsonStr = null;

        String status = "upcoming";

        String time = "&date=,1w";

        String key = "463f4e2e2d37672b55636f9636921";

        int numDays = 7;

        try {

            ////URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");
            ////URL url = new URL("https://api.getevents.co/event?&lat=51.50853&lng=9.93988&limit=3");
            ////URL url = new URL("https://api.getevents.co/event?&lat=51.50853&lng=-0.12574&limit=3");


            final String ITINERARY_BASE_URL = "https://api.meetup.com/2/open_events.json?date=,1w";
            final String COUNTRY_PARAM = "country";
            final String CITY_PARAM = "city";
            ////final String STATUS_PARAM= "status";
            final String TIME_PARAM = "time";
            final String KEY_PARAM = "key";

            Uri builtUri = Uri.parse(ITINERARY_BASE_URL).buildUpon()
                    .appendQueryParameter(COUNTRY_PARAM, params[0])
                    .appendQueryParameter(CITY_PARAM, params[1])
                            //.appendQueryParameter(STATUS_PARAM, status)
                            //.appendQueryParameter(TIME_PARAM,time)
                    .appendQueryParameter(KEY_PARAM, key)
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
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            itineraryJsonStr = buffer.toString();
            Log.v(LOG_TAG, "Itinerary JSON String" + itineraryJsonStr);

        } catch (IOException e) {
            Log.e("ItineraryFragment", "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
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
            return getScheduleDataFromJson(itineraryJsonStr, numDays, locationSetting, cityname);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

}
***/