package edu.sjsu.android.sleeptracker;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SleepPeriodDAO
{

    @Insert
    public void addData(SleepPeriod data);

    @Update
    public void updateData(SleepPeriod data);

    @Delete
    public void deleteData(SleepPeriod data);

    @Query("SELECT * FROM SleepPeriod")
    List<SleepPeriod> getAllSleepPeriodData();

    @Query("SELECT * FROM SleepPeriod WHERE startTime >= :last24Hours ORDER BY startTime DESC LIMIT 1")
    SleepPeriod getMostRecentSleepPeriod(long last24Hours);

    @Query("SELECT * FROM SleepPeriod WHERE Date BETWEEN :startweek AND :endweek")
    List<SleepPeriod> getAllSleepPeriodWeek(long startweek, long endweek);

    @Query("SELECT * FROM SleepPeriod WHERE Date BETWEEN :startMonth AND :endMonth")
    List<SleepPeriod> getAllSleepPeriodMonth(long startMonth, long endMonth);



}
