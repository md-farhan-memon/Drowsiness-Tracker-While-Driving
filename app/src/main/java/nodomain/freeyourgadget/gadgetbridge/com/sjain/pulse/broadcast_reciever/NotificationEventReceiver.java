package nodomain.freeyourgadget.gadgetbridge.com.sjain.pulse.broadcast_reciever;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.Calendar;
import java.util.Date;

import nodomain.freeyourgadget.gadgetbridge.com.sjain.pulse.notification.NotificationIntentService;

public class NotificationEventReceiver extends WakefulBroadcastReceiver implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String ACTION_START_NOTIFICATION_SERVICE = "ACTION_START_NOTIFICATION_SERVICE";
    private static final String ACTION_DELETE_NOTIFICATION = "ACTION_DELETE_NOTIFICATION";
    private static final String ACTION_RESET_ALARM_SERVICE_REST = "ACTION_RESET_ALARM_SERVICE_REST";
    private static final String ACTION_RESET_ALARM_SERVICE_DRIVE = "ACTION_RESET_ALARM_SERVICE_DRIVE";
    private static final String ACTION_RESET_ALARM_SERVICE_BOTH = "ACTION_RESET_ALARM_SERVICE_BOTH";
    public int MIN_SPEED = 25;

    public Location previousLoc;


    private static final int NOTIFICATIONS_INTERVAL_IN_HOURS = 2;

    public GoogleApiClient mGoogleApiClient;


    public synchronized void buildGoogleApiClient(Context mContext) {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    public static void setupAlarmForDrive(Context context) {
        AlarmManager alarmManagerDrive = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = getStartPendingIntent(context);
        alarmManagerDrive.setRepeating(AlarmManager.RTC_WAKEUP, getTriggerAt(new Date()), NOTIFICATIONS_INTERVAL_IN_HOURS * AlarmManager.INTERVAL_HOUR, alarmIntent);

    }

    public static void setupAlarmForRest(Context context) {
        AlarmManager alarmManagerRest = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = getStartPendingIntent(context);
        alarmManagerRest.setRepeating(AlarmManager.RTC_WAKEUP, getTriggerAt(new Date()), 10, alarmIntent);

    }


    public static void cancelAlarmRest(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent alarmIntent = getStartPendingIntent(context);
        alarmManager.cancel(alarmIntent);
    }


    private static long getTriggerAt(Date now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        //calendar.add(Calendar.HOUR, NOTIFICATIONS_INTERVAL_IN_HOURS);
        return calendar.getTimeInMillis();
    }

    private static PendingIntent getStartPendingIntent(Context context) {
        Intent intent = new Intent(context, NotificationEventReceiver.class);
        intent.setAction(ACTION_START_NOTIFICATION_SERVICE);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static PendingIntent getDeleteIntent(Context context) {
        Intent intent = new Intent(context, NotificationEventReceiver.class);
        intent.setAction(ACTION_DELETE_NOTIFICATION);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Intent serviceIntent = null;
        if (ACTION_START_NOTIFICATION_SERVICE.equals(action)) {
            serviceIntent = NotificationIntentService.createIntentStartNotificationService(context);
        } else if (ACTION_DELETE_NOTIFICATION.equals(action)) {
            Log.i(getClass().getSimpleName(), "onReceive delete notification action, starting notification service to handle delete");
            serviceIntent = NotificationIntentService.createIntentDeleteNotification(context);
        }

        if (serviceIntent != null) {
            startWakefulService(context, serviceIntent);
        }
    }

    //Location Scenario
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        checkLocation();
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
        if (previousLoc != null) {
            double dist = previousLoc.distanceTo(location);
            double time = (location.getElapsedRealtimeNanos() - previousLoc.getElapsedRealtimeNanos()) / 1000000000.0;
            double speed = (dist / time) * 3.6;
            Log.d("speed", speed + " km/hr");
        }
        previousLoc = location;
        Log.d("Location", location.getLatitude() + " , " + location.getLongitude());
    }


    @SuppressLint("MissingPermission")
    public void checkLocation() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(4000); //4sec
        mLocationRequest.setFastestInterval(2000); //2 Sec.
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }


}
