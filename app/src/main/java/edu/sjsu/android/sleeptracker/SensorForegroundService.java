package edu.sjsu.android.sleeptracker;

import android.app.Service;
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
