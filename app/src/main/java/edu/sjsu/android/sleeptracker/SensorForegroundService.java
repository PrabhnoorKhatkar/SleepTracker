package edu.sjsu.android.sleeptracker;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;

public class SensorForegroundService extends Service implements SensorEventListener
{

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private BatteryManager batteryManager;

    @Override
    public void onCreate()
    {
        super.onCreate();

        // Initialize SensorManager and light sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        batteryManager = (BatteryManager) getSystemService(Context.BATTERY_SERVICE);

        // Register listener to get sensor values
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);


        createNotificationChannel();
        //startForeground(1, getNotification());


    }

    private void createNotificationChannel() {

    }

    private Notification getNotification() {

        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT)
        {
            float luxValue = sensorEvent.values[0];

            // Get the battery status
            int batteryStatus = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS);

            SleepData sleepData = new SleepData(new Timestamp(System.currentTimeMillis()), luxValue, batteryStatus);
            //TODO Add sleepData to Database




        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    // Unused
    }
}
