package com.example.android.sunshine.app;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by praveen on 4/1/2016.
 */
public class DetailsFragment extends Fragment {
    private static final String LOG_TAG = DetailsFragment.class.getSimpleName();

    private static final String Forecast_Share_Hasgtag = "#SunshineApp";
    private String mforecastStr;

    public DetailsFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View RootView = inflater.inflate(R.layout.details_fragment, container, false);
        mforecastStr = getActivity().getIntent().getExtras().get("FORECAST").toString();
        //Toast.makeText(RootView.getContext(), "listiemclick form detailactivity" +m1, Toast.LENGTH_SHORT).show();
        TextView text = (TextView) RootView.findViewById(R.id.detailstextView);
        text.setText(mforecastStr);


        return RootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.details_fragment_share_menu, menu);
        // Retrieve the share menu item
        MenuItem menuItem = menu.findItem(R.id.action_share);
        // Get the provider and hold onto it to set/change the share intent.
        ShareActionProvider mShareActionProvider =
                (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        // Attach an intent to this ShareActionProvider.  You can update this at any time,
        // like when the user selects a new piece of data they might like to share.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareFroecatsIntent());
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null?");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_share) {

        }


        return true;
    }

    private Intent createShareFroecatsIntent() {

        Intent sharIntent = new Intent(Intent.ACTION_SEND);
        sharIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        sharIntent.setType("text/plain");
        sharIntent.putExtra(Intent.EXTRA_TEXT, mforecastStr + Forecast_Share_Hasgtag);

        return sharIntent;
    }


}
