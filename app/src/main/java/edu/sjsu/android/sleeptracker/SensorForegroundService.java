package edu.sjsu.android.sleeptracker;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;


public class SensorForegroundService extends Service implements SensorEventListener
{

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private BatteryManager batteryManager;
    private Handler handler;
    private long lastLoggedTime = 0;
    private long logInterval = 300000; // 5 Minutes = 300000milliseconds


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


    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    "SensorForegroundServiceChannel",
                    "Sensor Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }


    private Notification getNotification() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(this, "SensorForegroundServiceChannel")
                .setContentTitle("Sleep Tracker Running")
                .setContentText("Measuring Sleep")
                .setContentIntent(pendingIntent)
                .build();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT)
        {
            float luxValue = sensorEvent.values[0];

            // Get the battery status
            int batteryStatus = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS);

            // Debounce sensor readings - only log/store data every 5 minutes
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastLoggedTime >= logInterval) {
                lastLoggedTime = currentTime;
                Log.d("SensorDataService", "Lux Value: " + luxValue);

                //SleepData sleepData = new SleepData(new Timestamp(System.currentTimeMillis()), luxValue, batteryStatus);
                //TODO Add sleepData to Database
            }

        }
    }


    // Unused Methods
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
