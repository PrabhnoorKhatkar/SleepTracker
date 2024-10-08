package edu.sjsu.android.sleeptracker;


import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.Room;

@Database(entities = {SleepData.class}, version = 1)
public abstract class SleepDatabase extends RoomDatabase {
    public abstract SleepDataDAO sleepDataDAO();
}
