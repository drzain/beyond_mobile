package absen.youngdev.com.beyonddev;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener, OnMapReadyCallback {

    // LogCat tag
    boolean doubleBackToExitPressedOnce = false;
    private SessionManager session;
    private SessionNew newsession;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final String TAG = MainActivity.class.getSimpleName();
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    private Location mLastLocation;

    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    // boolean flag to toggle periodic location updates
    private boolean mRequestingLocationUpdates = false;
    private boolean mockLocationsEnabled = false;
    private LocationRequest mLocationRequest;

    // Location updates intervals in sec
    private static int UPDATE_INTERVAL = 10000; // 10 sec
    private static int FATEST_INTERVAL = 5000; // 5 sec
    private static int DISPLACEMENT = 10; // 10 meters
    private GoogleMap map;

    private static final LatLng KRANGGAN1 = new LatLng(-6.3409128, 106.9227058);
    private static final LatLng KRANGGAN3 = new LatLng(-6.340534, 106.92313);
    private static final LatLng KRANGGAN4 = new LatLng(-6.340816, 106.9228199);
    private static final LatLng MEDAN = new LatLng(3.5407691, 98.6385971);
    private static final LatLng MALANG = new LatLng(-7.9985731, 112.6187645);
    private static final LatLng SEMARANG = new LatLng(-6.98746, 110.3926908);
    private static final LatLng PUSAT = new LatLng(-6.3420113, 106.9181532);

    private Marker mKR1;
    private Marker mKR3;
    private Marker mKR4;
    private Marker mMDN;
    private Marker mMLG;
    private Marker mSMG;

    // UI elements
    private TextView lblLocation, lblUser;
    private Button btnShowLocation, btnStartLocationUpdates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        session = new SessionManager(getApplicationContext());
        if(!session.isLoggedIn()){
            Logout();
        }

        TextView lblTimeIn = (TextView) findViewById(R.id.checkintime);
        TextView lblTimeOut = (TextView) findViewById(R.id.checkouttime);
        TextView lblAtIn = (TextView) findViewById(R.id.checkinat);
        TextView lblAtOut = (TextView) findViewById(R.id.checkoutat);
        TextView lbltgl = (TextView) findViewById(R.id.tgl_absen);
        lblUser = (TextView) findViewById(R.id.lblnama);
        lblLocation = (TextView) findViewById(R.id.lblLocation);
        btnShowLocation = (Button) findViewById(R.id.btnShowLocation);
        btnStartLocationUpdates = (Button) findViewById(R.id.btnLocationUpdates);
        newsession = new SessionNew(getApplicationContext());
        lblTimeIn.setText(newsession.isTimeIn());
        lblTimeOut.setText(newsession.isTimeOut());
        lblAtIn.setText(newsession.isAtIn());
        lblAtOut.setText(newsession.isAtOut());
        lbltgl.setText(newsession.isTgl());
        if (newsession.isCheck() == false) {
            btnStartLocationUpdates
                    .setText("CHECK IN");
        } else {
            btnStartLocationUpdates
                    .setText("CHECK OUT");
        }
        // First we need to check availability of play services
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();

            createLocationRequest();
        }

        lblUser.setText(session.isNama());
        // Show location button click listener
        btnShowLocation.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                displayLocation();
            }
        });

        // Toggling the periodic location updates
        btnStartLocationUpdates.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                togglePeriodicLocationUpdates();
            }
        });

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //buildGoogleApiClient();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void Logout(){
        session.setLogin(false);
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public static boolean isMockSettingsON(Context context) {
        // returns true if mock location enabled, false if not enabled.
        if (Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ALLOW_MOCK_LOCATION).equals("0"))
            return false;
        else
            return true;
    }

    private void checkMockLocations() {
        checkLocationPermission();
        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);
        // Starting with API level >= 18 we can (partially) rely on .isFromMockProvider()
        // (http://developer.android.com/reference/android/location/Location.html#isFromMockProvider%28%29)
        // For API level < 18 we have to check the Settings.Secure flag
        if (Build.VERSION.SDK_INT < 18 &&
                !android.provider.Settings.Secure.getString(this.getContentResolver(), android.provider.Settings
                        .Secure.ALLOW_MOCK_LOCATION).equals("0")) {
            mockLocationsEnabled = true;
        } else if (Build.VERSION.SDK_INT > 18 && mLastLocation.isFromMockProvider()){
            mockLocationsEnabled = true;
        } else {
            mockLocationsEnabled = false;
        }
    }

    public static boolean areThereMockPermissionApps(Context context) {
        int count = 0;

        PackageManager pm = context.getPackageManager();
        List<ApplicationInfo> packages =
                pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo applicationInfo : packages) {
            try {
                PackageInfo packageInfo = pm.getPackageInfo(applicationInfo.packageName,
                        PackageManager.GET_PERMISSIONS);

                // Get Permissions
                String[] requestedPermissions = packageInfo.requestedPermissions;

                if (requestedPermissions != null) {
                    for (int i = 0; i < requestedPermissions.length; i++) {
                        if (requestedPermissions[i]
                                .equals("android.permission.ACCESS_MOCK_LOCATION")
                                && !applicationInfo.packageName.equals(context.getPackageName())) {
                            count++;
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                Log.e("Got exception " , e.getMessage());
            }
        }

        if (count > 0)
            return true;
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
                Log.d(TAG, "Minta Permission!");

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
                Log.d(TAG, "ini else ya!");
            }
            return false;
        } else {
            return true;
        }
    }



    @Override
    protected void onResume() {
        super.onResume();

        checkPlayServices();

        // Resuming the periodic location updates
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    /**
     * Method to display the location on UI
     * */
    private void displayLocation() {
        checkLocationPermission();
        startLocationUpdates();
        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            lblLocation.setText(latitude + ", " + longitude);

        } else {

            lblLocation
                    .setText("(Couldn't get the location. Make sure location is enabled on the device)");
        }
    }


    /**
     * Method to toggle periodic location updates
     * */
    private void togglePeriodicLocationUpdates() {
        newsession = new SessionNew(getApplicationContext());
        if (newsession.isCheck() == false) {
            // Changing the button text
            //btnStartLocationUpdates.setText(getString(R.string.btn_stop_location_updates));

            //newsession.setCheck(true);
            // Starting the location updates
            //startLocationUpdates();
            SendAbsenMasuk();

            Log.d(TAG, "Periodic location updates started!");

        } else {
            // Changing the button text
            //btnStartLocationUpdates.setText(getString(R.string.btn_start_location_updates));

            //newsession.setCheck(false);
            // Stopping the location updates
            SendAbsenPulang();

            Log.d(TAG, "Periodic location updates stopped!");
        }
    }

    private void SendAbsenMasuk(){
        String username = session.isEmail();
        String type = "I";
        String fake = "";

        checkMockLocations();
        if (mockLocationsEnabled == true) {
            fake = "Y";
        }else{
            fake = "N";
        }
        checkLocationPermission();
        double latitude = mLastLocation.getLatitude();
        double longitude = mLastLocation.getLongitude();
        String lat = Double.toString(latitude);
        String longi = Double.toString(longitude);

        new AsyncAbsen().execute(username, lat, longi, type, fake);
    }

    private void SendAbsenPulang(){
        String username = session.isEmail();
        String type = "O";
        String fake = "";
        checkMockLocations();
        if (mockLocationsEnabled == true) {
            fake = "Y";
        }else{
            fake = "N";
        }
        checkLocationPermission();

        double latitude = mLastLocation.getLatitude();
        double longitude = mLastLocation.getLongitude();
        String lat = Double.toString(latitude);
        String longi = Double.toString(longitude);

        new AsyncAbsen().execute(username, lat, longi, type, fake);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
        map = googleMap;
        checkLocationPermission();
        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(PUSAT, 5));


        mKR1 = map.addMarker(new MarkerOptions()
                .position(KRANGGAN1)
                .title("Kranggan 1"));
        mKR1.setTag(0);
        mKR3 = map.addMarker(new MarkerOptions()
                .position(KRANGGAN3)
                .title("Kranggan 3"));
        mKR3.setTag(0);
        mKR4 = map.addMarker(new MarkerOptions()
                .position(KRANGGAN4)
                .title("Kranggan 4"));
        mKR4.setTag(0);
        mMDN = map.addMarker(new MarkerOptions()
                .position(MEDAN)
                .title("MEDAN"));
        mMDN.setTag(0);
        mMLG = map.addMarker(new MarkerOptions()
                .position(MALANG)
                .title("MALANG"));
        mMLG.setTag(0);
        mSMG = map.addMarker(new MarkerOptions()
                .position(SEMARANG)
                .title("SEMARANG"));
        mSMG.setTag(0);

    }

    private class AsyncAbsen extends AsyncTask<String, String, JSONObject>
    {
        ProgressDialog pdLoading = new ProgressDialog(MainActivity.this);
        JSONParser jsonParser = new JSONParser();
        private static final String TAG_INFO = "info";
        private static final String TAG_ATIN = "lokasiin";
        private static final String TAG_ATOUT = "lokasiout";
        private static final String TAG_TIMEIN = "timein";
        private static final String TAG_TIMEOUT = "timeout";
        private static final String TAG_TGL = "tgl_absen";
        private static final String DAFTAR_URL = "http://172.16.31.199:1999/C_Absen";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();

        }

        @Override
        protected JSONObject doInBackground(String... args) {
            try {

                HashMap<String, String> params = new HashMap<>();
                params.put("username", args[0]);
                params.put("latitude", args[1]);
                params.put("longitude", args[2]);
                params.put("type", args[3]);
                params.put("fake", args[4]);


                Log.d("request", "starting");

                JSONObject json = jsonParser.makeHttpRequest(
                        DAFTAR_URL, "POST", params);

                if (json != null) {
                    Log.d("JSON result", json.toString());

                    return json;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(JSONObject json) {
            String info = "";
            String timein = "";
            String timeout = "";
            String atin = "";
            String atout = "";
            String tgl = "";


            if (json != null) {
                //Toast.makeText(LoginActivity.this, json.toString(),
                //Toast.LENGTH_LONG).show();

                try {
                    info = json.getString(TAG_INFO);
                    timein = json.getString(TAG_ATIN);
                    timeout = json.getString(TAG_ATOUT);
                    atin = json.getString(TAG_TIMEIN);
                    atout = json.getString(TAG_TIMEOUT);
                    tgl = json.getString(TAG_TGL);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            if(info.equals("success")) {
                pdLoading.dismiss();
                /*Toast.makeText(MainActivity.this, "Absen Berhasil!",
                        Toast.LENGTH_LONG).show();*/
                //newsession.setCheck(true);
                newsession.setTimeIn(timein);
                newsession.setTimeOut(timeout);
                newsession.setAtIn(atin);
                newsession.setAtOut(atout);
                newsession.setTgl(tgl);
                TextView lblTimeIn = (TextView) findViewById(R.id.checkintime);
                TextView lblTimeOut = (TextView) findViewById(R.id.checkouttime);
                TextView lblAtIn = (TextView) findViewById(R.id.checkinat);
                TextView lblAtOut = (TextView) findViewById(R.id.checkoutat);
                TextView lbltgl = (TextView) findViewById(R.id.tgl_absen);
                lblTimeIn.setText(newsession.isTimeIn());
                lblTimeOut.setText(newsession.isTimeOut());
                lblAtIn.setText(newsession.isAtIn());
                lblAtOut.setText(newsession.isAtOut());
                lbltgl.setText(newsession.isTgl());
                Calendar c = Calendar.getInstance();
                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                String formattedDate = df.format(c.getTime());
                newsession = new SessionNew(getApplicationContext());
                if (newsession.isCheck() == false) {
                    newsession.setCheck(true);
                    newsession.setDate(formattedDate);
                    btnStartLocationUpdates.setText("CHECK OUT");
                }else{
                    newsession.setCheck(false);
                    newsession.setDate(formattedDate);
                    btnStartLocationUpdates.setText("CHECK IN");
                }
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Sukses")
                        .setMessage("Absen Berhasil")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setNegativeButton(android.R.string.yes, null).show();
            }else if(info.equals("double")){
                pdLoading.dismiss();
                /*Toast.makeText(MainActivity.this, "Kamu Sudah Absen !!",
                        Toast.LENGTH_LONG).show();*/
                //newsession.setCheck(true);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Absen")
                        .setMessage("Kamu Sudah Absen !")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setNegativeButton(android.R.string.yes, null).show();
            }else{
                pdLoading.dismiss();
                /*Toast.makeText(MainActivity.this, "Absen Gagal !!",
                        Toast.LENGTH_LONG).show();*/
                //newsession.setCheck(true);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Absen")
                        .setMessage("Absen Gagal, Silakan Coba Lagi !")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setNegativeButton(android.R.string.yes, null).show();
            }

        }
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     * */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    /**
     * Method to verify google play services on the device
     * */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * Starting the location updates
     * */

    protected void startLocationUpdates() {
        checkLocationPermission();
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);

    }

    /**
     * Stopping location updates
     */
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }



    /*@Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            SharedPreferences pref = getSharedPreferences("BeyondDevLogin", 0);
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.commit();
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }*/



    /**
     * Google api callback methods
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + result.getErrorCode());
    }

    @Override
    public void onConnected(Bundle arg0) {
        //mGoogleApiClient.connect();
        // Once connected with google api, get the location
        displayLocation();
        startLocationUpdates();

        /*checkLocationPermission();
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        double lng = mLastLocation.getLongitude();
        double lat = mLastLocation.getLatitude();
        LatLng loc = new LatLng(lat,lng);
        map.addMarker(new MarkerOptions().position(loc)
                .title("Your Location"));
        map.moveCamera(CameraUpdateFactory.newLatLng(loc));*/
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        // Assign the new location
        mLastLocation = location;
        /*checkLocationPermission();
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            double lng = mLastLocation.getLongitude();
            double lat = mLastLocation.getLatitude();

            if (myLocatMarker != null) {
                myLocatMarker.remove();
            }
            LatLng ll = new LatLng(lat, lng);
            MarkerOptions markerOpt = new MarkerOptions().title("my location")
                    .position(ll);
            System.out.println("ABC onConnected map: " + lat + " ; " + lng);
            myLocatMarker = map.addMarker(markerOpt);
        }*/

        // Displaying the new location on UI
        displayLocation();
    }
}
