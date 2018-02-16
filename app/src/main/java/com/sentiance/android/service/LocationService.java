package com.sentiance.android.service;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.sentiance.android.receiver.ProximityIntentReceiver;

public class LocationService extends Service {

    private static final int FIFTEEN_MINUTES = 1000 * 60 * 15;
    private static final String PROX_ALERT_INTENT =
            "com.sentiance.android.receiver.ProximityAlert";

    private LocationManager locationManager;
    private MyLocationListener listener;
    private Location previousBestLocation = null;

    private ProximityIntentReceiver mProximityIntentReceiver;

    private double markedLatitude;
    private double markedLongitude;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            listener = new MyLocationListener();

            // check if location permission was granted
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 300000, 10, listener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 300000, 10, listener);
            }

            markedLatitude = intent.getDoubleExtra("latitude", 0);
            markedLongitude = intent.getDoubleExtra("longitude", 0);
            int radius = Integer.parseInt(intent.getStringExtra("radius"));

            addProximityAlert(markedLatitude, markedLongitude, radius);
        } catch (Exception e) {
            Log.d(getClass().getName(), e.getMessage());
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > FIFTEEN_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -FIFTEEN_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than fifteen minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two fifteen older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }


    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        // remove location updates
        locationManager.removeUpdates(listener);

        // unregister the proximity receiver
        unregisterReceiver(mProximityIntentReceiver);
    }

    /**
     * Add proximity alert broadcast receiver. It will notify if user enter/exit within certain area.
     *
     * @param latitude  Latitude
     * @param longitude Longitude
     * @param radius    Radius
     */
    private void addProximityAlert(double latitude, double longitude, int radius) {
        Intent intent = new Intent(PROX_ALERT_INTENT);
        PendingIntent proximityIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.addProximityAlert(
                    latitude, // the latitude of the central point of the alert region
                    longitude, // the longitude of the central point of the alert region
                    radius, // the radius of the central point of the alert region, in meters
                    -1, // time for this proximity alert, in milliseconds, or -1 to indicate no expiration
                    proximityIntent // will be used to generate an Intent to fire when entry to or exit from the alert region is detected
            );

            mProximityIntentReceiver = new ProximityIntentReceiver();
            IntentFilter filter = new IntentFilter(PROX_ALERT_INTENT);
            registerReceiver(mProximityIntentReceiver, filter);

        }
    }

    private class MyLocationListener implements LocationListener {

        public void onLocationChanged(final Location loc) {
            if (isBetterLocation(loc, previousBestLocation)) {

                previousBestLocation = loc;

                double currentLatitude = loc.getLatitude();
                double currentLongitude = loc.getLongitude();

                // calculate the distance between two location
                // current location and marked location with the help of https://developer.android.com/reference/android/location/Location.html#distanceBetween(double,%20double,%20double,%20double,%20float[])

                float results[] = new float[1];
                Location.distanceBetween(currentLatitude, currentLongitude, markedLatitude, markedLongitude, results);
            }
        }

        public void onProviderDisabled(String provider) {
            Log.d(getClass().getName(), "Gps Disabled");
        }

        public void onProviderEnabled(String provider) {
            Log.d(getClass().getName(), "Gps Enabled");
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

    }
}