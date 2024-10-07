package edu.sjsu.android.sleeptracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class SensorForegroundService extends Service implements SensorEventListener
{

    private SensorManager sensorManager;
    private Sensor lightSensor;

    @Override
    public void onCreate()
    {
        super.onCreate();

        // Initialize SensorManager and light sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        // Register listener to get sensor values
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);



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
            //TODO Add to Database
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    // Unused
    }
}
