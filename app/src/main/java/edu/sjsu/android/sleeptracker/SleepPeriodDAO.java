package edu.sjsu.android.sleeptracker;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SleepPeriodDAO {

    @Insert
    public void addData(SleepPeriod data);

    @Update
    public void updateData(SleepPeriod data);

    @Delete
    public void deleteData(SleepPeriod data);

    @Query("SELECT * FROM SleepPeriod")
    List<SleepPeriod> getAllSleepPeriodData();
}
