package edu.sjsu.android.sleeptracker;


import android.content.Context;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.Room;
import androidx.room.TypeConverters;

@Database(entities = {SleepData.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class SleepDatabase extends RoomDatabase {

    public abstract SleepDataDAO sleepDataDAO();
    private static volatile SleepDatabase INSTANCE;


    public static SleepDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SleepDatabase.class)
            {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), SleepDatabase.class, "sleep_database").build();
                }
            }
        }
        return INSTANCE;
    }
}