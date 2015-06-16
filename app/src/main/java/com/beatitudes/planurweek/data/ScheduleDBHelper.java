package com.beatitudes.planurweek.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.beatitudes.planurweek.data.ScheduleContract.LocationEntry;
import com.beatitudes.planurweek.data.ScheduleContract.ScheduleEntry;

/**
 * Created by user on 04-06-2015.
 */
public class ScheduleDBHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "schedule.db";


    public ScheduleDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold locations.  A location consists of the string supplied in the
        // location setting, the city name, and the latitude and longitude
        final String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE " + LocationEntry.TABLE_NAME + " (" +
                LocationEntry._ID + " INTEGER PRIMARY KEY," +
                ////LocationEntry.COLUMN_LOCATION_SETTING + " TEXT UNIQUE NOT NULL, " +
                LocationEntry.COLUMN_LOCATION_SETTING + " TEXT NOT NULL, " +
                ////LocationEntry.COLUMN_CITY_NAME + " TEXT NOT NULL, " +
                LocationEntry.COLUMN_COUNTRY_CODE + " TEXT NOT NULL " +


                " );";

        final String SQL_CREATE_SCHEDULE_TABLE = "CREATE TABLE " + ScheduleEntry.TABLE_NAME + " (" +

                ScheduleEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +


                ScheduleEntry.COLUMN_LOC_KEY + " INTEGER NOT NULL, " +
                ScheduleEntry.COLUMN_DATETEXT + " TEXT NOT NULL, " +
                ScheduleEntry.COLUMN_EVENT_NAME + " TEXT NOT NULL, " +


                ScheduleEntry.COLUMN_GROUP_NAME + " TEXT NOT NULL, " +
                ScheduleEntry.COLUMN_EVENT_URL + " TEXT NOT NULL, " +

                ScheduleEntry.COLUMN_EVENT_TIME + " REAL NOT NULL, " +
//                ScheduleEntry.COLUMN_VENUE_NAME + " TEXT NOT NULL, " +
//                ScheduleEntry.COLUMN_VENUE_ADDRESS1 + " TEXT NOT NULL, " +
//                ScheduleEntry.COLUMN_VENUE_CITY + " TEXT NOT NULL, " +

                // Set up the location column as a foreign key to location table.
                " FOREIGN KEY (" + ScheduleEntry.COLUMN_LOC_KEY + ") REFERENCES " +
                LocationEntry.TABLE_NAME + " (" + LocationEntry._ID + "), " +

                // To assure the application have just one schedule entry per day
                // per location, it's created a UNIQUE constraint with REPLACE strategy
                " UNIQUE (" + ScheduleEntry.COLUMN_DATETEXT + ", " +
                ScheduleEntry.COLUMN_LOC_KEY + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_LOCATION_TABLE);
        sqLiteDatabase.execSQL(SQL_CREATE_SCHEDULE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + LocationEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + ScheduleEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
