package com.beatitudes.planurweek;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.beatitudes.planurweek.data.ScheduleContract;

import java.text.DateFormat;

/**
 * Created by user on 14-06-2015.
 */

    public class Utility {
        public static String getPreferredLocation(Context context) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            return prefs.getString(context.getString(R.string.pref_location_key),
                    context.getString(R.string.pref_location_default));
        }
        public static String getPreferredCountryCode(Context context){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            return prefs.getString(context.getString(R.string.pref_countrycode_key),
                    context.getString(R.string.pref_countrycode_default));
        }

    static String formatDate(String dateString) {
        java.util.Date date = ScheduleContract.getDateFromDb(dateString);
        return DateFormat.getDateInstance().format(date);
    }
}
