package edu.sjsu.android.sleeptracker;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Timestamp;

@Entity(tableName = "sleepPeriod")
public class SleepPeriod
{
    @PrimaryKey(autoGenerate = true)
    private int SleepID;

    @ColumnInfo(name = "Date")
    private Timestamp date;

    @ColumnInfo(name = "sleepDuration")
    private float duration;

    @ColumnInfo(name = "startTime")
    private Timestamp startTime;

    @ColumnInfo(name = "endTime")
    private Timestamp endTime;


    public SleepPeriod() {
    }

    public SleepPeriod(Timestamp date, float duration, Timestamp startTime, Timestamp endTime) {
        this.date = date;
        this.duration = duration;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }
}
