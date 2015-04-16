package com.vkassin.ftracker;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class GPSTracker extends IntentService implements LocationListener {

    private static Context mContext;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 6 * 1; // 1 minute = 1000 * 60 * 1

    private static String URL = "http://89.107.99.238:10356";
    private static String URL1 = "http://192.168.2.168";

    // Declaring a Location Manager
    protected LocationManager locationManager;
    private String deviceID = "not initialized";

    public GPSTracker(Context context) {

        super("GPSTracker");

        mContext = context;
//        getLocation();
    }
    public GPSTracker(String name) {
        super(name);
    }

    public GPSTracker() {

        super("GPSTracker");
//        mContext = getApplicationContext();


    }

    @Override
    final protected void onHandleIntent(Intent intent) {

        mContext = getApplicationContext();
//        deviceID = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
        deviceID = tm.getDeviceId();
        if (canGetLocation()) {

            double latitude = getLatitude();
            double longitude = getLongitude();

            // \n is for new line
//                    Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
            Log.i("position", "Your Location is - Lat: " + latitude + " Long: " + longitude + " " + deviceID);
            stopUsingGPS();

            sendToServer(latitude, longitude);

        }
    }

    private void sendToServer(double lat, double lon) {

        try{

            // URLEncode user defined data

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String currentDT = sdf.format(new Date());

            String dtime    = URLEncoder.encode("\""+currentDT+"\"", "UTF-8");
            String device    = URLEncoder.encode("\""+deviceID+"\"", "UTF-8");

            String URL_ADD = "/gps_track.php?device="+device+"&lat="+lat+"&lon="+lon+"&cl_time="+dtime;
//            String convertedURL = URLEncoder.encode(URL, "UTF-8");

            Log.i("position", URL);

           sendUrl(URL_ADD, false);
        }
        catch(UnsupportedEncodingException ex)
        {
            Log.i("position", "http create Failed!!");
        }
    }

    private boolean sendUrl(String url_add, boolean fromStore) {

        // Create http cliient object to send request to server
        boolean b = false;

        HttpClient Client = new DefaultHttpClient();

        try
        {
            // Create Request to server and get response

            HttpGet httpget = new HttpGet(URL + url_add);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String SetServerString = Client.execute(httpget, responseHandler);

            // Show response on activity

            Log.i("position", SetServerString);

//            if(!fromStore) {
//
//                sendStore(url_add);
//            }

            b = true;
        }
        catch(Exception ex)
        {
            Log.i("position", "http Failed!!");
            Log.i("position", ex.toString());

//            if(!fromStore) {
//
//                addToStore(url);
//            }
        }

        if(!b)
        try
        {
            // Create Request to server and get response

            HttpGet httpget = new HttpGet(URL1 + url_add);
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            String SetServerString = Client.execute(httpget, responseHandler);

            // Show response on activity

            Log.i("position", SetServerString);

//            if(!fromStore) {
//
//                sendStore(url_add);
//            }

            b = true;
        }
        catch(Exception ex)
        {
            Log.i("position", "http Failed!!");
            Log.i("position", ex.toString());

        }

            if(!fromStore) {

                if(!b)
                    addToStore(url_add);
                else
                    sendStore();
            }

        return b;
    }

    private void saveStoreToFile(List<String> list) {

        FileOutputStream fos;
        try {

            fos = mContext.openFileOutput("store.ftr", Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(list);
            os.close();
            fos.close();

        } catch (FileNotFoundException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();
        }

    }

    private List<String> loadStoreFromFile() {

        List<String> list = new ArrayList<String>();
        FileInputStream fileInputStream;
        try {

            fileInputStream = mContext.openFileInput("store.ftr");
            ObjectInputStream oInputStream = new ObjectInputStream(
                    fileInputStream);
            Object one = oInputStream.readObject();
            list = (List<String>) one;
            oInputStream.close();
            fileInputStream.close();

        } catch (FileNotFoundException e) {

            Log.i("position", "no stored file");

        } catch (StreamCorruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return list;
    }

    private void sendStore() {

        List<String> list = loadStoreFromFile();
        for (Iterator<String> iterator = list.iterator(); iterator.hasNext(); ) {
            String url = iterator.next();
            if (sendUrl(url, true)) {

                iterator.remove();
            }
        }
        saveStoreToFile(list);
    }

    private void addToStore(String url) {

        List<String> list = loadStoreFromFile();
        list.add(url);
        saveStoreToFile(list);
    }

    private Location getLocation() {
        try {
            this.canGetLocation = false;

            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

//            Log.i("position", locationManager.getAllProviders().toString());
//            Criteria criteria = new Criteria();
//            criteria.setAccuracy(Criteria.ACCURACY_FINE);
//            criteria.setAltitudeRequired(false);
//            criteria.setBearingRequired(false);
//            criteria.setCostAllowed(true);
//            criteria.setPowerRequirement(Criteria.POWER_LOW);
//            final String bestProvider = locationManager.getBestProvider(criteria, true);
//            Log.i("position", "best provider: " + bestProvider);
//
//            locationManager.requestLocationUpdates(
//                    bestProvider,
//                    MIN_TIME_BW_UPDATES,
//                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
//            if (locationManager != null) {
//                location = locationManager
//                        .getLastKnownLocation(bestProvider);
//                if (location != null) {
//
//                    this.canGetLocation = true;
//                    latitude = location.getLatitude();
//                    longitude = location.getLongitude();
//                }
//            }

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
                Log.i("position", "no network provider is enabled");

            } else {
//                this.canGetLocation = true;
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.i("position", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            this.canGetLocation = true;
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.i("position", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                this.canGetLocation = true;
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     * */
    public void stopUsingGPS(){
        if(locationManager != null){
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * Function to get latitude
     * */
    public double getLatitude(){
        if(location != null){
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     * */
    public double getLongitude(){
        if(location != null){
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    /**
     * Function to check GPS/wifi enabled
     * @return boolean
     * */
    public boolean canGetLocation() {

        getLocation();

        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {

        Log.i("position", "onLocationChanged");
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        Log.i("position", "123 Your Location is - Lat: " + latitude + " Long: " + longitude);
    }


    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

}