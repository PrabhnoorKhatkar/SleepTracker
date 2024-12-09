package edu.sjsu.android.sleeptracker;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Timestamp;

@Entity(tableName = "SleepData")
public class SleepData {

    @PrimaryKey(autoGenerate = true)
    private int eventID;

    @ColumnInfo(name = "Time")
    private Timestamp timestamp;

    @ColumnInfo(name = "Lux")
    private float lux;

    @ColumnInfo(name = "ChargeState")
    private int chargeState;

    public SleepData(Timestamp timestamp, float lux, int chargeState) {
        this.timestamp = timestamp;
        this.lux = lux;
        this.chargeState = chargeState;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public float getLux() {
        return lux;
    }

    public boolean isCharging() {
        return chargeState == 4;
    }
}
