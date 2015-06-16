package com.beatitudes.planurweek;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beatitudes.planurweek.data.ScheduleContract;

public class DetailActivity extends ActionBarActivity {


    private static final String LOCATION_KEY = "location";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

  @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

@Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

        private static final String LOG_TAG = DetailFragment.class.getSimpleName();
        private static final String ITINERARY_SHARE_HASHTAG = "#planurweek";
//        private String mItineraryStr;

        public static final String DATE_KEY = "Itinerary_date";

        private ShareActionProvider mShareActionProvider;
        private String mLocation;
        private String mItinerary;
		private static final int DETAIL_LOADER = 0;


        private static final String[] ITINERARY_COLUMNS = {
                ScheduleContract.ScheduleEntry.TABLE_NAME + "." + ScheduleContract.ScheduleEntry._ID,
                ScheduleContract.ScheduleEntry.COLUMN_DATETEXT,
                ScheduleContract.ScheduleEntry.COLUMN_EVENT_NAME,
                ScheduleContract.ScheduleEntry.COLUMN_GROUP_NAME,
                ScheduleContract.ScheduleEntry.COLUMN_EVENT_URL,
        };

        public DetailFragment() {
            setHasOptionsMenu(true);
        }


       @Override
        public void onSaveInstanceState(Bundle outState) {
            outState.putString(LOCATION_KEY, mLocation);
            super.onSaveInstanceState(outState);
        }

		@Override
        public void onResume() {
            super.onResume();
            if (mLocation != null &&
                    !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
                getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {


		return inflater.inflate(R.layout.fragment_detail, container, false);
        }


        private Intent createShareItineraryIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mItinerary + " " + ITINERARY_SHARE_HASHTAG);
            return shareIntent;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
			 Log.v(LOG_TAG, "inside onCreateOptionsMenu");
            // Inflate the menu; this adds items to the action bar if it is present.
            ////getMenuInflater().inflate(R.menu.menu_detail, menu);
            inflater.inflate(R.menu.detailfragment, menu);
            MenuItem menuItem = menu.findItem(R.id.action_share);
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

            if (mItinerary != null) {
                mShareActionProvider.setShareIntent(createShareItineraryIntent());
            } else {
                Log.d(LOG_TAG, "Share Action Provider is null? ");
            }


        }

		@Override
        public void onActivityCreated(Bundle savedInstanceState) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
            if (savedInstanceState != null) {
                mLocation = savedInstanceState.getString(LOCATION_KEY);
			}
			 super.onActivityCreated(savedInstanceState);
        
		}


        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.v(LOG_TAG, "In onCreateLoader");
            Intent intent = getActivity().getIntent();
            if (intent == null || !intent.hasExtra(DATE_KEY)) {
                return null;
            }
            String itineraryDate = intent.getStringExtra(DATE_KEY);

            // Sort order:  Ascending, by date.
            String sortOrder = ScheduleContract.ScheduleEntry.COLUMN_DATETEXT + " ASC";

            mLocation = Utility.getPreferredLocation(getActivity());
            Uri scheduleForLocationUri = ScheduleContract.ScheduleEntry.buildScheduleLocationWithDate(
                    mLocation, itineraryDate);
            Log.v(LOG_TAG, scheduleForLocationUri.toString());


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
            Log.v(LOG_TAG, "In onLoadFinished");
            if (!data.moveToFirst()) { return; }

            String dateString = Utility.formatDate(
                    data.getString(data.getColumnIndex(ScheduleContract.ScheduleEntry.COLUMN_DATETEXT)));
            ((TextView) getView().findViewById(R.id.detail_date_textview))
                    .setText(dateString);

            String eventDescription =
                    data.getString(data.getColumnIndex(ScheduleContract.ScheduleEntry.COLUMN_EVENT_NAME));
            ((TextView) getView().findViewById(R.id.detail_eventname_textview))
                    .setText(eventDescription);


            String grpnameString = data.getString(data.getColumnIndex(ScheduleContract.ScheduleEntry.COLUMN_GROUP_NAME));
            ((TextView) getView().findViewById(R.id.detail_grpname_textview))
                    .setText(grpnameString);

            String eventURL =
                    data.getString(data.getColumnIndex(ScheduleContract.ScheduleEntry.COLUMN_EVENT_URL));
            ((TextView) getView().findViewById(R.id.detail_eventurl_textview))
                    .setText(eventURL);


            // We still need this for the share intent
            mItinerary = String.format("%s - %s - %s/%s", dateString, eventDescription, grpnameString, eventURL);

            Log.v(LOG_TAG, "Itinerary String: " + mItinerary);

            // If onCreateOptionsMenu has already happened, we need to update the share intent now.
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(createShareItineraryIntent());
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) { }
    }
}


