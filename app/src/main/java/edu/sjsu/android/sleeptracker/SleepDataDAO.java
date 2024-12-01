package edu.sjsu.android.sleeptracker;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SleepDataDAO {

    @Insert
    void addData(SleepData data);

    @Query("SELECT * FROM SleepData")
    List<SleepData> getAllSleepData();
}
