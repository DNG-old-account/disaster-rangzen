/*
 * Copyright (c) 2014, De Novo Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.denovogroup.rangzen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.denovogroup.rangzen.FragmentOrganizer.FragmentType;
import org.denovogroup.rangzen.RangeSeekBar.OnRangeSeekBarChangeListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.AdapterView;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.StateListDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * This class is in control of all Google maps related activity except for
 * allowing the fragment to maintain itself on orientation changes. This class
 * is also in charge of programmatic features including onClickListeners for the
 * map interface.
 */
public class MapsActivity extends Fragment implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener, OnClickListener {

    /** Displayed in Android Log messages. */
    private static final String TAG = "MapsActivity";

    /**
     * The fragment that contains the map itself. It is the second most top
     * fragment in its parent FrameLayout.
     */
    private SupportMapFragment mMapFragment;

    /**
     * Variable that will last the length of the activity, so the map will not
     * be centered gain while the activity is up.
     */
    private static boolean mHasCentered = false;

    /**
     * The map object itself that is inside of the SupportMapFragment.
     */
    private GoogleMap mMap;
    /**
     * Used to connect and disconnect from Google Location Services and
     * locations.
     */
    private LocationClient mLocationClient;
    /**
     * The transparent fragment. It is the bottom most layer of the FrameLayout
     * but is brought to the top if created.
     */
    private Fragment mTransparent;
    /**
     * This is the object that manages all of the Fragments visible and
     * invisible, and adds, replaces or removes fragments.
     */
    private FragmentManager mFragmentManager;

    /**
     * Handle to Rangzen storage manager.
     */
    private StorageBase mStore;

    /** Used to determine if the slider is on or off. */
    private static boolean mIsSliderOn = false;

    /** RangeBar slider that will determine how many points will be shown. */
    private RangeSeekBar<Double> mPolyLineRange;

    /**
     * Define a request code to send to Google Play services This code is
     * returned in Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /**
     * Polyline ArrayList in order to hold all polyline options as the next one
     * is built.
     */
    private ArrayList<Polyline> mPolylineArray = new ArrayList<Polyline>();

    /** Size of the current polyline. */
    private int mSizeofPolyline = 0;

    /** Number of running Async Tasks. */
    private static int mNumAsyncTasks = 0;

    /** List of exchanges for this current map. */
    private ArrayList<Marker> mMarkers = new ArrayList<Marker>();

    // /** Helps with determining what functionality the back button should
    // have. */
    // private static boolean mAboutShowing = false;

    /** Percent change that the slider should change by. */
    private final int mIntegerPercentChange = 10;

    private Bitmap bitmap1;
    private Bitmap bitmap2;
    private Bitmap bitmap3;
    private Bitmap bitmap4;
    private Bitmap bitmap5;
    private Bitmap bitmap1a;
    private Bitmap bitmap2a;
    private Bitmap bitmap3a;
    private Bitmap bitmap4a;
    private Bitmap bitmap5a;

    private View view;

    /**
     * Sets up the initial FragmentManager and if there is no savedInstanceState
     * for this app then new fragments are created for the map interface.
     * 
     * @param savedInstanceState
     *            This is the memory of the last state of this application.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // setContentView(R.layout.master);
        view = inflater.inflate(R.layout.master, container, false);
        mLocationClient = new LocationClient(getActivity(), this, this);

        if (savedInstanceState == null) {

            mStore = new StorageBase(getActivity(),
                    StorageBase.ENCRYPTION_DEFAULT);
            mLocationStore = new LocationStore(getActivity(),
                    StorageBase.ENCRYPTION_NONE);
            mMapFragment = (SupportMapFragment) SupportMapFragment
                    .newInstance();
            // mMapFragment.setRetainInstance(true);
            mMap = mMapFragment.getMap();
            mFragmentManager = getActivity().getSupportFragmentManager();
            mFragmentManager.beginTransaction()
                    .replace(R.id.mapHolder, mMapFragment).commit();
            // createTransparentFragment();
        }

        if (mMapFragment == null) {
            setUpMapIfNeeded();
            if (mMapFragment == null) {
                Toast.makeText(getActivity(), "map fragment was null but creatinga new one", Toast.LENGTH_SHORT).show();
                mMapFragment = (SupportMapFragment) SupportMapFragment
                        .newInstance();
                // mMapFragment.setRetainInstance(true);
                mMap = mMapFragment.getMap();
                mFragmentManager = getActivity().getSupportFragmentManager();
                mFragmentManager.beginTransaction()
                        .replace(R.id.mapHolder, mMapFragment).commit();
            }
            mMap.setMyLocationEnabled(true);
        }
        return view;
    }

    /** This will be used to debug the location services being turned on and off. */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO (jesus) Find out why the location services is not coming back on.
        super.onActivityCreated(savedInstanceState);
        setUpMapIfNeeded();
        if (mMap != null) { 
            //mMap.setMyLocationEnabled(true);
        }
    }

    /**
     * Manually garbage collect and unbind the drawables in this map frame so
     * they can be collected.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mHasCentered = false;
        unbindDrawables(getActivity().findViewById(R.id.mapFrame));
        System.gc();
    }

    /**
     * Recursively go through the views in this view and unbind their
     * backgrounds so they can be garbage collected.
     * 
     * @param view
     *            In this case it will be map frame, the ultimate parent UI
     *            frame.
     */
    private void unbindDrawables(View view) {
        if (view != null && view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup && !(view instanceof AdapterView)) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            ((ViewGroup) view).removeAllViews();
        }
    }

    /**
     * Will only be called in OnCreate and also the app needs to have been
     * destroyed in order for the method to be called again.
     */
    private void centerMap() {
        Location location = mLocationClient.getLastLocation();
        LatLng latLng;
        if (location == null) {
            latLng = new LatLng(0, 0);
        } else {
            latLng = new LatLng(location.getLatitude(), location.getLongitude());

        }

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,
                15);
        setUpMapIfNeeded();
        if (mMap != null) {
            if (!mHasCentered) {
                mMap.animateCamera(cameraUpdate);
                mHasCentered = true;
            }
            //Toast.makeText(getActivity(), "location enabled in centermap", Toast.LENGTH_SHORT).show();
            mMap.setMyLocationEnabled(true);
            mMap.setBuildingsEnabled(true);
        }
    }

    /**
     * Called when Rangzen is first visible and this connects to the google
     * services.
     */
    @Override
    public void onStart() {
        super.onStart();
        // connect the client
        if (isGooglePlayServicesAvailable()) {
            mLocationClient.connect();
        }
    }

    /**
     * Called when the Activity is no longer visible.
     */
    @Override
    public void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        setUpMapIfNeeded();
        //mMap.setMyLocationEnabled(false);
        super.onStop();
    }

    /** Kill all the bitmaps. */
    public void recycleBitmaps() {
        if (bitmap1 != null && bitmap3 != null && bitmap5 != null
                && bitmap2a != null) {
            bitmap1.recycle();
            bitmap1a.recycle();
            bitmap2.recycle();
            bitmap2a.recycle();
            bitmap3.recycle();
            bitmap3a.recycle();
            bitmap4.recycle();
            bitmap4a.recycle();
            bitmap5.recycle();
            bitmap5a.recycle();

            bitmap1 = null;
            bitmap1a = null;
            bitmap2 = null;
            bitmap2a = null;
            bitmap3 = null;
            bitmap3a = null;
            bitmap4 = null;
            bitmap4a = null;
            bitmap5 = null;
            bitmap5a = null;
        }
    }

    /**
     * Checks to see if GooglePlayServices is available; if it is then it
     * returns true, if not then an error is given and it returns false.
     * 
     * @return True or False depending on the readyness of google play services.
     */
    private boolean isGooglePlayServicesAvailable() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getActivity());
        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {
            return true;
        } else {
            // Get the error dialog from Google Play services
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(
                    resultCode, getActivity(),
                    CONNECTION_FAILURE_RESOLUTION_REQUEST);

            // If Google Play services can provide an error dialog
            if (errorDialog != null) {
                // Create a new DialogFragment for the error dialog
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(errorDialog);
                errorFragment.show(getActivity().getSupportFragmentManager(),
                        "Location Updates");
            }

            return false;
        }
    }

    /**
     * Called by Location Services when the request to connect the client
     * finishes successfully. At this point, you can request the current
     * location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        if (!mHasCentered) {
            centerMap();
        }
        setUpMapIfNeeded();
        
        mMap.setMyLocationEnabled(true);
    }

    /**
     * Called by Location Services when the request to connect the client
     * finishes successfully. At this point, you can request the current
     * location or start periodic updates
     */
    @Override
    public void onDisconnected() {
        if (!mHasCentered) {
            centerMap();
        }
    }

    /** Define a DialogFragment that displays the error dialog */
    public static class ErrorDialogFragment extends DialogFragment {

        /** Global field to contain the error dialog */
        private Dialog mDialog;

        /** Default constructor. Sets the dialog field to null */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /** Set the dialog to display */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /** Return a Dialog to the DialogFragment. */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }

    /**
     * Unimplemented methods that need to be here for implementing other
     * classes.
     */
    @Override
    public void onConnectionFailed(ConnectionResult arg0) {

    }

    @Override
    public void onPause() {
        //mMap.setMyLocationEnabled(false);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        } else {
            Toast.makeText(getActivity(), "map was null in onResume",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /** Time between location updates in milliseconds - 1 minute. */
    private static final long LOCATION_UPDATE_INTERVAL = 5000 * 60 * 1;

    /**
     * Minimum moved distance between location updates. We want a new location
     * even if we haven't moved, so we set it to 0.
     * */
    private static final float LOCATION_UPDATE_DISTANCE_MINIMUM = 0;

    /** Handle to Rangzen location storage provider. */
    private LocationStore mLocationStore;

    /** Radius of the Erf (Earth). */
    private int radius = 6371000;

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play
     * services APK is correctly installed) and the map has not already been
     * instantiated. This will ensure that we only ever manipulate the map once
     * when it is not null.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (mMap == null) {
            mMapFragment = ((SupportMapFragment) getActivity()
                    .getSupportFragmentManager().findFragmentById(
                            R.id.mapHolder));
            if (mMapFragment != null) {
                mMap = mMapFragment.getMap();
            }
        }
    }

    @Override
    public void onClick(View v) {
        int viewID = v.getId();
        // if (viewID == R.id.refresh) {
        //     sRefresh(v);
        // }
    }
}
