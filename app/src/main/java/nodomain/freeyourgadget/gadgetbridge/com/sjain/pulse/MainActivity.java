package nodomain.freeyourgadget.gadgetbridge.com.sjain.pulse;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.com.sjain.pulse.utils.CustomBluetoothProfile;

import static nodomain.freeyourgadget.gadgetbridge.com.sjain.pulse.FetchAddressIntentService.RESULT_DATA_KEY;

@SuppressLint("NewApi")
public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    double latitude;
    double longitude;
    Boolean isListeningHeartRate = false;

    LineChart mLineChart;

    BluetoothAdapter bluetoothAdapter;
    BluetoothGatt bluetoothGatt;
    BluetoothDevice bluetoothDevice;

    GoogleApiClient mGoogleApiClient;
    public static int PERMISSION_ALL = 111;
    private static final int FAILURE_RESULT = 1;
    AddressResultReceiver mResultReceiver;
    Status status;
    Double speed = 0.0;
    Double MIN_SPEED = 25.0;
    int MINHEARTRATE = 60;
    CountDownTimer mTimerDrive, mTimerRest;


    private Location mPreviousLoc = null;
    public LocationRequest mLocationRequest;
    private PendingResult<LocationSettingsResult> pendingResult;

    private static long restTimer = 5 * 60 * 1000;
    private static long DriveTimer = 2 * 60 * 60 * 1000;

    private boolean drivetimerRunning = false;
    private boolean resttimerRunning = false;
    AlertDialog alert, alert1, alertDrowzy;


    ArrayList<Entry> heartRate;

    TextView tvHeartRate, tvSpeed;

    Boolean isConnected = false;

    Integer drowzyCount = 0;
//    int dummyValue = 60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvHeartRate = (TextView) findViewById(R.id.tv_heart_rate);
        tvSpeed = (TextView) findViewById(R.id.tv_speed);
        mLineChart = (LineChart) findViewById(R.id.line_chart);
        mLineChart.getDescription().setEnabled(false);

        // enable touch gestures
        mLineChart.setTouchEnabled(true);

        mLineChart.setDragDecelerationFrictionCoef(0.9f);

        // enable scaling and dragging
        mLineChart.setDragEnabled(true);
        mLineChart.setScaleEnabled(true);
        mLineChart.setDrawGridBackground(false);
        mLineChart.setHighlightPerDragEnabled(true);

        // set an alternative background color
        mLineChart.setBackgroundColor(Color.WHITE);
        mLineChart.setViewPortOffsets(0f, 0f, 0f, 0f);
        buildGoogleApiClient();
        /*for (int i = 0; i < 25; i++) {
            int random = new Random().nextInt(100 - 65) + 65;
            if (heartRate == null) {
                heartRate = new ArrayList<>();
                heartRate.add(new Entry(i, random));
            } else {
                heartRate.add(new Entry(i, random));
            }
        }*/

    }

    void initializeObjects() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    // To prevent crash on resuming activity  : interaction with fragments allowed only after Fragments Resumed or in OnCreate
    // http://www.androiddesignpatterns.com/2013/08/fragment-transaction-commit-state-loss.html
    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        // handleIntent();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        askPermission();
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

    public void askPermission() {
        String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_PRIVILEGED};
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        } else {
            checkLocation();
            initializeObjects();
            getBoundedDevice();
        }
    }

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {

            boolean locationPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (locationPermission) {
                checkLocation();
                initializeObjects();
                getBoundedDevice();
            } else {
                Toast.makeText(MainActivity.this, "All permissions are required", Toast.LENGTH_SHORT).show();
            }
        }
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

        if (mPreviousLoc != null) {
            double dist = mPreviousLoc.distanceTo(location);
//            Log.d("Dist Covered", dist + " m");
            double time = (location.getElapsedRealtimeNanos() - mPreviousLoc.getElapsedRealtimeNanos()) / 1000000000.0;
//            Log.d("time", time + " s");
            speed = (dist / time) * 3.6;
            tvSpeed.setText(String.format("%.1f", speed) + " km/hr");
            if (speed > MIN_SPEED) {
                if (!drivetimerRunning) {
                    timerDrive();
                } else {
                    if (mTimerRest != null) {
                        mTimerRest.cancel();
                        resttimerRunning = false;
                    }
                }

            } else {
                if (!resttimerRunning && drivetimerRunning) {
                    timerRest();
                } else {

                }
            }

            Log.d("SPEED", speed + " km/hr");

        }
        mPreviousLoc = location;


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


    public void timerDrive() {
        mTimerDrive = new CountDownTimer(DriveTimer, 1000) {
            @Override
            public void onTick(long l) {
                drivetimerRunning = true;
            }

            @Override
            public void onFinish() {
                createAlertStopDrive();
            }
        }.start();
    }

    private void createAlertStopDrive() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Driving for 2 hours");
        builder.setMessage("Take a break!!!").setCancelable(false).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        alert.dismiss();
                        resetTimer();
                    }
                });
        if (alert == null || alert != null && !alert.isShowing()) {
            alert = builder.create();
            alert.show();
        }
    }

    private void resetTimer() {
        drivetimerRunning = false;
        resttimerRunning = false;

    }

    private void createAlertStartDrive() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Start Drive");
        builder.setMessage("You have relaxed and refreshed can start drive now").setCancelable(false).setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        alert1.dismiss();
                        resetTimer();
                    }
                });
        if (alert1 == null || alert1 != null && !alert1.isShowing()) {
            alert1 = builder.create();
            alert1.show();
        }
    }

    public void timerRest() {
        mTimerRest = new CountDownTimer(restTimer, 1000) {
            @Override
            public void onTick(long l) {
                resttimerRunning = true;
            }

            @Override
            public void onFinish() {
                //RESET timer
                createAlertStartDrive();
                if (mTimerDrive != null) {
                    mTimerDrive.cancel();
                }
                drivetimerRunning = false;

            }
        }.start();

    }


    //Bluetooth To measure heart rate methods


    void getBoundedDevice() {
        Set<BluetoothDevice> boundedDevice = bluetoothAdapter.getBondedDevices();
        for (BluetoothDevice bd : boundedDevice) {
            if (bd.getName().contains("MI Band 2")) {
                bluetoothDevice = bluetoothAdapter.getRemoteDevice(bd.getAddress());
                Log.v("test", "Connecting to " + bd.getAddress());
                Log.v("test", "Device name " + bluetoothDevice.getName());
                bluetoothGatt = bluetoothDevice.connectGatt(this, false, new BluetoothGattCallback() {

                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                        super.onConnectionStateChange(gatt, status, newState);
                        Log.v("test", "onConnectionStateChange");

                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            stateConnected();
                            callTimerMethod();
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            stateDisconnected();
                        }

                    }

                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        super.onServicesDiscovered(gatt, status);
                        Log.v("test", "onServicesDiscovered");
                        listenHeartRate();
                    }

                    @Override
                    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                        super.onCharacteristicRead(gatt, characteristic, status);
                        byte[] data = characteristic.getValue();
                        updateTextViewValues(Arrays.toString(data));
                    }

                    @Override
                    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                        super.onCharacteristicWrite(gatt, characteristic, status);
                        Log.v("test", "onCharacteristicWrite");
                    }

                    @Override
                    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                        super.onCharacteristicChanged(gatt, characteristic);
                        Log.v("test", "onCharacteristicChanged");
                        byte[] data = characteristic.getValue();
                        /*dummyValue = dummyValue - 3;
                        data[1] = (byte) (dummyValue);*/
                        updateChart(String.valueOf(data[1]));
                        updateTextViewValues(String.valueOf(data[1]));
                        if (speed > MIN_SPEED) {
                            updateIsDrowzy(Integer.valueOf(data[1]));
                        } else {
                            drowzyCount = 0;
                        }
                    }

                    @Override
                    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                        super.onDescriptorRead(gatt, descriptor, status);
                        Log.v("test", "onDescriptorRead");
                    }

                    @Override
                    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                        super.onDescriptorWrite(gatt, descriptor, status);
                        Log.v("test", "onDescriptorWrite");
                    }

                    @Override
                    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
                        super.onReliableWriteCompleted(gatt, status);
                        Log.v("test", "onReliableWriteCompleted");
                    }

                    @Override
                    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                        super.onReadRemoteRssi(gatt, rssi, status);
                        Log.v("test", "onReadRemoteRssi");
                    }

                    @Override
                    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                        super.onMtuChanged(gatt, mtu, status);
                        Log.v("test", "onMtuChanged");
                    }

                });
            }
        }
    }

    private void updateIsDrowzy(Integer currenthRate) {
        if (currenthRate < MINHEARTRATE) {
            drowzyCount++;
            if (drowzyCount == 5) {
                alertDrowzy();
            }
        } else {
            drowzyCount = 0;
        }
    }

    private void alertDrowzy() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Feeling Drowzy");
                builder.setMessage("Take a break!!!").setCancelable(false).setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                alertDrowzy.dismiss();
                                drowzyCount = 0;
                            }
                        });
                if (alertDrowzy == null || alertDrowzy != null && !alertDrowzy.isShowing()) {
                    alertDrowzy = builder.create();
                    alertDrowzy.show();
                }
            }
        });
    }

    private void updateChart(String s) {
        if (heartRate == null) {
            heartRate = new ArrayList<>();
            heartRate.add(new Entry(0, Float.valueOf(s)));
            callLegendSetting();
        } else {
            heartRate.add(new Entry(heartRate.size(), Float.valueOf(s)));
        }
        LineDataSet set1 = new LineDataSet(heartRate, "DataSet 1");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);
        set1.setColor(ColorTemplate.getHoloBlue());
        set1.setValueTextColor(ColorTemplate.getHoloBlue());
        set1.setLineWidth(2f);
        set1.setDrawCircles(false);
        set1.setDrawValues(false);
        set1.setFillAlpha(65);
        set1.setFillColor(ColorTemplate.getHoloBlue());
        set1.setDrawCircleHole(false);
        final LineData data = new LineData(set1);
        data.setValueTextColor(Color.BLACK);
        data.setValueTextSize(9f);


        /*if (heartRate.size() > 5) {
            List<Entry> lastfiveEntry = heartRate.subList(heartRate.size() - 5, heartRate.size());
            for (int j = 0; j < lastfiveEntry.size(); j++) {
                if (Integer.valueOf((int) lastfiveEntry.get(j).getY()) < MINHEARTRATE){

                }
            }
        }*/


        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mLineChart.setData(data);
                mLineChart.invalidate();
            }
        });


    }

    public void callLegendSetting() {
        // get the legend (only possible after setting data)
        Legend l = mLineChart.getLegend();
        l.setEnabled(false);

        XAxis xAxis = mLineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(true);
        xAxis.setTextColor(Color.rgb(255, 192, 56));
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularity(5f); // one hour
        xAxis.setValueFormatter(new IAxisValueFormatter() {

            private SimpleDateFormat mFormat = new SimpleDateFormat("HH:mm:ss");

            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                long millis = TimeUnit.SECONDS.toMillis((long) System.currentTimeMillis());
                return mFormat.format(new Date(millis));
            }
        });

        YAxis leftAxis = mLineChart.getAxisLeft();
        leftAxis.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        leftAxis.setTextColor(ColorTemplate.getHoloBlue());
        leftAxis.setDrawGridLines(true);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(150f);
        leftAxis.setYOffset(-9f);
        leftAxis.setTextColor(Color.rgb(255, 192, 56));
        YAxis rightAxis = mLineChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    public void stateConnected() {
        bluetoothGatt.discoverServices();
        //   updateTextViewValues("Connected");
    }

    void stateDisconnected() {

        /*bluetoothGatt.disconnect();
        updateTextViewValues("DisConnected");*/
    }


    void startScanHeartRate() {
        if (bluetoothGatt != null) {
            Log.e("HEllo", "SOLVED");
            BluetoothGattCharacteristic bchar = bluetoothGatt.getService(CustomBluetoothProfile.HeartRate.service)
                    .getCharacteristic(CustomBluetoothProfile.HeartRate.controlCharacteristic);
            if (bchar != null) {
                bchar.setValue(new byte[]{21, 2, 1});
                bluetoothGatt.writeCharacteristic(bchar);
            }
        } else {
            Log.e("HEllo", "PROBLEM");
        }
    }


    void listenHeartRate() {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(CustomBluetoothProfile.HeartRate.service)
                .getCharacteristic(CustomBluetoothProfile.HeartRate.measurementCharacteristic);
        bluetoothGatt.setCharacteristicNotification(bchar, true);
        BluetoothGattDescriptor descriptor = bchar.getDescriptor(CustomBluetoothProfile.HeartRate.descriptor);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);
        isListeningHeartRate = true;
    }

    void startVibrate() {
        BluetoothGattCharacteristic bchar = bluetoothGatt.getService(CustomBluetoothProfile.AlertNotification.service)
                .getCharacteristic(CustomBluetoothProfile.AlertNotification.alertCharacteristic);
        bchar.setValue(new byte[]{2});
        if (!bluetoothGatt.writeCharacteristic(bchar)) {
            Toast.makeText(this, "Failed start vibrate", Toast.LENGTH_SHORT).show();
        }
    }

    void callTimerMethod() {

        Timer timer = new Timer();
        TimerTask hourlyTask = new TimerTask() {
            @Override
            public void run() {
                Log.e("Called", "METHOD");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startScanHeartRate();
                    }
                });

            }
        };
        timer.schedule(hourlyTask, 0, 5000);
    }

    public void updateTextViewValues(final String message) {

        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Integer.valueOf(message) < MINHEARTRATE) {
                    tvHeartRate.setTextColor(Color.parseColor("#ff0000"));
                } else {
                    tvHeartRate.setTextColor(Color.parseColor("#008000"));
                }
                if (Integer.valueOf(message) > 0) {
                    tvHeartRate.setText(message);
                } else {
                    tvHeartRate.setText("0");
                }

            }
        });

    }


}
