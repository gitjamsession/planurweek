package com.beatitudes.planurweek.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by user on 04-06-2015.
 */
public class ScheduleProvider extends ContentProvider {

    private ScheduleDBHelper mOpenHelper;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int SCHEDULE = 100;
    private static final int SCHEDULE_WITH_LOCATION = 101;
    private static final int SCHEDULE_WITH_LOCATION_AND_DATE = 102;
    private static final int LOCATION = 300;
    private static final int LOCATION_ID = 301;

    private static final SQLiteQueryBuilder sScheduleByLocationSettingQueryBuilder;

    static{
        sScheduleByLocationSettingQueryBuilder = new SQLiteQueryBuilder();
        sScheduleByLocationSettingQueryBuilder.setTables(
                ScheduleContract.ScheduleEntry.TABLE_NAME + " INNER JOIN " +
                        ScheduleContract.LocationEntry.TABLE_NAME +
                        " ON " + ScheduleContract.ScheduleEntry.TABLE_NAME +
                        "." + ScheduleContract.ScheduleEntry.COLUMN_LOC_KEY +
                        " = " + ScheduleContract.LocationEntry.TABLE_NAME +
                        "." + ScheduleContract.LocationEntry._ID);
    }

    private static final String sLocationSettingSelection =
            ScheduleContract.LocationEntry.TABLE_NAME+
                    "." + ScheduleContract.LocationEntry.COLUMN_CITY_NAME + " = ? ";
    private static final String sLocationSettingWithStartDateSelection =
            ScheduleContract.LocationEntry.TABLE_NAME+
                    "." + ScheduleContract.LocationEntry.COLUMN_CITY_NAME + " = ? AND " +
                    ScheduleContract.ScheduleEntry.COLUMN_DATETEXT + " >= ? ";

    private static final String sLocationSettingAndDaySelection =
            ScheduleContract.LocationEntry.TABLE_NAME +
                    "." + ScheduleContract.LocationEntry.COLUMN_CITY_NAME + " = ? AND " +
                    ScheduleContract.ScheduleEntry.COLUMN_DATETEXT + " = ? ";

    private Cursor getScheduleByLocationSetting(Uri uri, String[] projection, String sortOrder) {
        String locationSetting = ScheduleContract.ScheduleEntry.getLocationSettingFromUri(uri);
        String startDate = ScheduleContract.ScheduleEntry.getStartDateFromUri(uri);

        String[] selectionArgs;
        String selection;

        if (startDate == null) {
            selection = sLocationSettingSelection;
            selectionArgs = new String[]{locationSetting};
        } else {
            selectionArgs = new String[]{locationSetting, startDate};
            selection = sLocationSettingWithStartDateSelection;
        }

        return sScheduleByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getScheduleByLocationSettingAndDate(
            Uri uri, String[] projection, String sortOrder) {
        String locationSetting = ScheduleContract.ScheduleEntry.getLocationSettingFromUri(uri);
        String date = ScheduleContract.ScheduleEntry.getDateFromUri(uri);

        return sScheduleByLocationSettingQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sLocationSettingAndDaySelection,
                new String[]{locationSetting, date},
                null,
                null,
                sortOrder
        );
    }



    private static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = ScheduleContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, ScheduleContract.PATH_SCHEDULE, SCHEDULE);
        matcher.addURI(authority, ScheduleContract.PATH_SCHEDULE + "/*", SCHEDULE_WITH_LOCATION);
        matcher.addURI(authority, ScheduleContract.PATH_SCHEDULE + "/*/*", SCHEDULE_WITH_LOCATION_AND_DATE);

        matcher.addURI(authority, ScheduleContract.PATH_LOCATION, LOCATION);
        matcher.addURI(authority, ScheduleContract.PATH_LOCATION + "/#", LOCATION_ID);

        return matcher;
    }

    @Override
    public boolean onCreate(){
        mOpenHelper = new ScheduleDBHelper(getContext());
        return true;

    }

    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case SCHEDULE: {
                long _id = db.insert(ScheduleContract.ScheduleEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = ScheduleContract.ScheduleEntry.buildScheduleUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case LOCATION: {
                long _id = db.insert(ScheduleContract.LocationEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = ScheduleContract.LocationEntry.buildLocationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SCHEDULE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(ScheduleContract.ScheduleEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case SCHEDULE:
                rowsDeleted = db.delete(
                        ScheduleContract.ScheduleEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case LOCATION:
                rowsDeleted = db.delete(
                        ScheduleContract.LocationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case SCHEDULE:
                rowsUpdated = db.update(ScheduleContract.ScheduleEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case LOCATION:
                rowsUpdated = db.update(ScheduleContract.LocationEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    public String getType(Uri uri){
        final int match = sUriMatcher.match(uri);

        switch (match){

            case SCHEDULE_WITH_LOCATION_AND_DATE:
                return ScheduleContract.ScheduleEntry.CONTENT_ITEM_TYPE;

            case SCHEDULE_WITH_LOCATION:
                return ScheduleContract.ScheduleEntry.CONTENT_TYPE;

            case SCHEDULE:
                return ScheduleContract.ScheduleEntry.CONTENT_TYPE;

            case LOCATION:
                return ScheduleContract.LocationEntry.CONTENT_TYPE;


            default:
                throw new UnsupportedOperationException("Unknown uri"+uri);

        }

    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {

            case SCHEDULE_WITH_LOCATION_AND_DATE:
            {
                retCursor = getScheduleByLocationSettingAndDate(uri, projection, sortOrder);
                break;
            }

            case SCHEDULE_WITH_LOCATION: {
                retCursor = getScheduleByLocationSetting(uri, projection, sortOrder);
                break;
            }

            case SCHEDULE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ScheduleContract.ScheduleEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case LOCATION_ID: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ScheduleContract.LocationEntry.TABLE_NAME,
                        projection,
                        ScheduleContract.LocationEntry._ID + " = '" + ContentUris.parseId(uri) + "'",
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            case LOCATION: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        ScheduleContract.LocationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }



}
