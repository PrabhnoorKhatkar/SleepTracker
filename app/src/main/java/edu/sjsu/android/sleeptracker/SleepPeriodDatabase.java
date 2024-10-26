package edu.sjsu.android.sleeptracker;

import android.content.Context;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.Room;
import androidx.room.TypeConverters;

@Database(entities = {SleepPeriod.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class SleepPeriodDatabase extends RoomDatabase {

    public abstract SleepPeriodDAO sleepPeriodDAO();
    private static volatile SleepPeriodDatabase INSTANCE;


    public static SleepPeriodDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (SleepPeriodDatabase.class)
            {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), SleepPeriodDatabase.class, "SleepPeriodDB").build();

                }
            }
        }
        return INSTANCE;
    }

    public void closeDatabase() {
        if (INSTANCE != null && INSTANCE.isOpen()) {
            INSTANCE.close();
        }
    }
}