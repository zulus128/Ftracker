package com.vkassin.ftracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

//http://stackoverflow.com/questions/7387294/run-gps-listener-in-background-android

public class MainActivity extends ActionBarActivity {

    // GPSTracker class
    GPSTracker gps = new GPSTracker(MainActivity.this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = (Button) findViewById(R.id.buttonHide);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                Log.i("position", "start pressed");


//                Timer timer=new Timer();
//                TimerTask tt=new TimerTask(){
//                    @Override
//                    public void run() {
//
//                        doGPS();
//                    }
//                };
//                timer.schedule(tt,0,20000);

                Calendar cal = Calendar.getInstance();

                AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                Intent notifyintent = new Intent(MainActivity.this, OnAlarmReceiver.class);
                notifyintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                notifyintent.setAction("android.intent.action.NOTIFY");
                PendingIntent notifysender = PendingIntent.getBroadcast(MainActivity.this, 0, notifyintent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                am.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 600 * 1000,
                        notifysender);
            }
        });

        final Button button1 = (Button) findViewById(R.id.buttonStop);
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.i("position", "stop pressed");



                AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                Intent notifyintent = new Intent(MainActivity.this, OnAlarmReceiver.class);
                notifyintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                notifyintent.setAction("android.intent.action.NOTIFY");
                PendingIntent notifysender = PendingIntent.getBroadcast(MainActivity.this, 0, notifyintent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                am.cancel(notifysender);
            }
        });



    }

    private void doGPS() {

        // check if GPS enabled
        if(gps.canGetLocation()){

            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();

            // \n is for new line
//                    Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
            Log.i("position", "Your Location is - Lat: " + latitude + " Long: " + longitude);
            gps.stopUsingGPS();
        }else{
//            gps.showSettingsAlert();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
