package edu.sjsu.android.sleeptracker;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SleepDataDAO {

    @Insert
    public void addData(SleepData data);

    @Update
    public void updateData(SleepData data);

    @Delete
    public void deleteData(SleepData data);

    @Query("SELECT * FROM SleepData")
    List<SleepData> getAllSleepData();
}
