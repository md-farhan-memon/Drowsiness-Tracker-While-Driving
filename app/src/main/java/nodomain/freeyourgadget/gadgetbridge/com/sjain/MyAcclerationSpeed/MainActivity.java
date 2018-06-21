package nodomain.freeyourgadget.gadgetbridge.com.sjain.MyAcclerationSpeed;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.com.sjain.MyAcclerationSpeed.interfaces.AccelerometerListener;
import nodomain.freeyourgadget.gadgetbridge.com.sjain.MyAcclerationSpeed.model.GeoLocation;
import nodomain.freeyourgadget.gadgetbridge.com.sjain.MyAcclerationSpeed.utilities.AccelerometerManager;
import nodomain.freeyourgadget.gadgetbridge.com.sjain.MyAcclerationSpeed.utilities.CallDetectService;
import nodomain.freeyourgadget.gadgetbridge.com.sjain.MyAcclerationSpeed.utilities.CallHelper;
import nodomain.freeyourgadget.gadgetbridge.com.sjain.MyAcclerationSpeed.utilities.FirebaseUtils;
import nodomain.freeyourgadget.gadgetbridge.com.sjain.MyAcclerationSpeed.utilities.GoogleClientConstant;

import static nodomain.freeyourgadget.gadgetbridge.com.sjain.pulse.FetchAddressIntentService.RESULT_DATA_KEY;

public class MainActivity extends AppCompatActivity implements AccelerometerListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback {


    private static final int FAILURE_RESULT = 1;
    public static float avgv = 0;
    public static double MAX_SPEED = 25.00;
    public static double PREV_SPEED = 0;
    public static int PERMISSION_ALL = 111;
    public LocationRequest mLocationRequest;
    public GoogleApiClient mGoogleApiClient;
    //Data
    TextView tvAccel, tvAccelXY, tvSpeed;
    Context ctx;
    TelephonyManager tm;
    DatabaseReference mDatabase;
    AddressResultReceiver mResultReceiver;
    Double latitude = null;
    Double longitude = null;
    Status status;
    ArrayList<GeoLocation> mGeoLocation;
    Double speed = 0.0;
    GoogleMap mGoogleMap;
    SupportMapFragment mapFragment;
    FrameLayout flFragment;
    Marker mCurrentLocation = null;
    AlertDialog alert;
    Integer mMaxSpeedAlert;
    Location mLocationAlert;
    Toast mToast;
    float mAccel = 0;
    //Location
    private PendingResult<LocationSettingsResult> pendingResult;
    private LatLng mLatlng;
    private Location mPreviousLoc = null;

    public static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_1);
        ctx = this;
        mDatabase = FirebaseUtils.getDatabase();
        tvAccel = findViewById(R.id.tv_acclerometer);
        tvSpeed = findViewById(R.id.tv_speed);
        flFragment = findViewById(R.id.fl_fragment);
        getData();
        buildGoogleApiClient();
        setDetectEnabled();
        callMapFragment();

    }

    private void callMapFragment() {
        mapFragment = SupportMapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fl_fragment, mapFragment, "MAP_FRAG");
        fragmentTransaction.commit();

        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
        mGoogleMap.getUiSettings().setAllGesturesEnabled(true);
        mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        LatLng india = new LatLng(20.7673977, 73.7012866);
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(india, 4));

        if (mGeoLocation != null) {
            plotBlackSpotsOnMap();
        }
    }

    public void plotBlackSpotsOnMap() {
        for (int i = 0; i < mGeoLocation.size(); i++) {
            GeoLocation mCurrent = mGeoLocation.get(i);
            mGoogleMap.addMarker(new MarkerOptions()
                    .title(mCurrent.getName())
                    .position(new LatLng(mCurrent.getLatitude(), mCurrent.getLongitude())));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (AccelerometerManager.isSupported(this)) {
            AccelerometerManager.startListening(this);
        }
    }

    @Override
    public void onAcceleration(float x, float y, float z, float accel) {
        if (accel > 3 && speed > MAX_SPEED) {
            mAccel = accel;
            showAToast("HARSH ACCELRATION");
            tvAccel.setText("\nAccelration : " + accel + "HARSH");
        }
        tvAccel.setText("\nAccelration : " + accel);
        Log.e("Accelration", accel + "m/s2");
        PREV_SPEED = speed;
    }

    public void showAToast(String message) {
        //"Toast toast" is declared in the class
        if (mToast != null) {
            mToast.getView().isShown();     // true if visible
            mToast.setText(message);
        } else {
            mToast = Toast.makeText(ctx, message, Toast.LENGTH_LONG);
        }
        mToast.show();  //finally display it
        /*if (mToast != null && mToast.getView().isShown()) {
            mToast.cancel();
            return;
        }
        mToast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        mToast.show();*/
    }

    @Override
    public void onStop() {
        super.onStop();

        //Check device supported Accelerometer senssor or not
        /*if (AccelerometerManager.isListening()) {

            //Start Accelerometer Listening
            AccelerometerManager.stopListening();
        }*/
    }

    public void askPermission() {
        String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE};
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            setDetectEnabled();
            checkLocation();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (AccelerometerManager.isListening()) {
            AccelerometerManager.stopListening();
        }
    }

    private void setDetectEnabled() {

        Intent intent = new Intent(this, CallDetectService.class);
        startService(intent);
    }


    //Connecting to location

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        askPermission();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @SuppressLint("NewApi")
    @Override
    public void onLocationChanged(Location location) {
        mLatlng = new LatLng(location.getLatitude(), location.getLongitude());
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        if (mCurrentLocation != null) {
            mCurrentLocation.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
        } else {
            MarkerOptions m = new MarkerOptions()
                    .position(new LatLng(location.getLatitude(), location.getLongitude()))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
            mCurrentLocation = mGoogleMap.addMarker(m);
        }
        if (mPreviousLoc != null) {
            double dist = mPreviousLoc.distanceTo(location);
            Log.d("Dist Covered", dist + " m");
            double time = (location.getElapsedRealtimeNanos() - mPreviousLoc.getElapsedRealtimeNanos()) / 1000000000.0;
//            Log.d("time", time + " s");
            speed = (dist / time) * 3.6;
            CallHelper.AVG_SPEED = speed;
            tvSpeed.setText("Speed : " + String.format("%.1f", speed) + " km/hr");
            if (speed > MAX_SPEED && (alert == null || (alert != null && !alert.isShowing()))) {
                callQueringFunction(location);
            } else {
                if (mMaxSpeedAlert != null && speed < mMaxSpeedAlert || mLocationAlert != null && location.distanceTo(mLocationAlert) > 2000) {
                    alert.dismiss();
                    mMaxSpeedAlert = null;
                    mLocationAlert = null;
                } else if (mMaxSpeedAlert != null || mLocationAlert != null) {
                    callSound();
                }
            }

        }
        mPreviousLoc = location;
    }

    private void callQueringFunction(Location mLoc) {
        //Log.e("called", "Called");
        if (mGeoLocation != null && mGeoLocation.size() > 0) {
            for (int i = 0; i < mGeoLocation.size(); i++) {
                Location dLoc = new Location("");
                dLoc.setLatitude(mGeoLocation.get(i).getLatitude());
                dLoc.setLongitude(mGeoLocation.get(i).getLongitude());
                double dist = mLoc.distanceTo(dLoc);
                //Log.e("distance " + i, dist + "m");
                //Log.e("speed " + i, mGeoLocation.get(i).getMin_speed() + "speed");
                if (dist <= 1000 && speed > mGeoLocation.get(i).getMin_speed()) {
                    mMaxSpeedAlert = mGeoLocation.get(i).getMin_speed();
                    mLocationAlert = dLoc;
                    showAlertDanger();
                    break;
                }
            }
        }

    }

    private void callSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAlertDanger() {
        // Log.e("called", "Called ALert");
        callSound();
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Danger");
        builder.setMessage("Your Ideal Speed should be 30 please slow down").setCancelable(false);
        if (alert == null || alert != null && !alert.isShowing()) {
            alert = builder.create();
            alert.show();
        }
    }

    //Request Permission For Location
    public void requestUserLocationPermission() {


        if (mResultReceiver == null)
            mResultReceiver = new AddressResultReceiver(new Handler());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {

            boolean locationPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            boolean phonePermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

            if (locationPermission && phonePermission) {
                checkLocation();
                setDetectEnabled();
            } else {
                Toast.makeText(ctx, "All permissions are required", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(ctx, "All permissions are required", Toast.LENGTH_SHORT).show();
        }
        /*if (requestCode == LOCATION_ACCESS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocation();
            } else {
                Toast.makeText(ctx, "Permission Denied", Toast.LENGTH_LONG).show();
            }

        }*/
    }

    synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @SuppressLint("MissingPermission")
    protected void checkLocation() {

        if (mGoogleApiClient.isConnected()) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(4000); //4sec
            mLocationRequest.setFastestInterval(2000); //2 Sec.
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            // building SettingApi for device state check (whether we have all necessary setting enabled in device)
            LocationSettingsRequest.Builder settingsRequest = new LocationSettingsRequest.Builder();
            settingsRequest.addLocationRequest(mLocationRequest);
            settingsRequest.setAlwaysShow(true); // This will hide 'Never' option from system dialog.
            settingsRequest.setNeedBle(true);

// - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            pendingResult = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, settingsRequest.build());
            pendingResult.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(@NonNull LocationSettingsResult result) {
                    status = result.getStatus();
                    final LocationSettingsStates settingsStates = result.getLocationSettingsStates();
//                    Log.d("Status", status.getStatusCode() + "");
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can initialize location requests here.
                            getLocationUpdate();
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user  a dialog.
                            // Show the dialog by calling startResolutionForResult() and check the result in onActivityResult().
                            try {
                                status.startResolutionForResult(MainActivity.this, GoogleClientConstant.REQUEST_LOCATION_ON_CODE);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }

                            break;


                        case LocationSettingsStatusCodes.CANCELED:
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            });
        } else {
            buildGoogleApiClient();
        }

    }

    public void getLocationUpdate() {
        mResultReceiver = new AddressResultReceiver(new Handler());
    }


    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    public void getData() {
        mGeoLocation = new ArrayList<>();
        mDatabase.child(FirebaseUtils.PATH_SYS_GEOFIRE_DATA).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    GeoLocation mCurrent = dataSnapshot1.getValue(GeoLocation.class);
                    mGeoLocation.add(mCurrent);
                }
                if (mGoogleMap != null) {
                    plotBlackSpotsOnMap();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(ctx, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    class AddressResultReceiver extends ResultReceiver {

        AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         * Receives data sent from FetchAddressIntentService and updates the UI.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == FAILURE_RESULT) {
            }
            Address address = resultData.getParcelable(RESULT_DATA_KEY);
            if (address != null) {
                latitude = address.getLatitude();
                longitude = address.getLongitude();
            }
        }
    }

}
