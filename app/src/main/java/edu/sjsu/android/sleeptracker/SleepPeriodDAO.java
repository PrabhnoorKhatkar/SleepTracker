package edu.sjsu.android.sleeptracker;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SleepPeriodDAO
{

    @Insert
    void addData(SleepPeriod data);

    @Update
    void updateData(SleepPeriod data);

    @Query("SELECT MAX(sleepDuration) FROM SleepPeriod")
    Float getMaxSleep();

    @Query("SELECT * FROM SleepPeriod WHERE Date = :date LIMIT 1")
    SleepPeriod getSleepPeriodByDate(long date);

    @Query("SELECT * FROM SleepPeriod ORDER BY startTime DESC LIMIT 1")
    SleepPeriod getMostRecentSleepPeriod();

    @Query("SELECT * FROM SleepPeriod WHERE Date BETWEEN :startweek AND :endweek")
    List<SleepPeriod> getAllSleepPeriodWeek(long startweek, long endweek);


}
