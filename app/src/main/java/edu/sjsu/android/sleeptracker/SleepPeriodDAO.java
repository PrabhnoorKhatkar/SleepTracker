package edu.sjsu.android.sleeptracker;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SleepPeriodDAO
{

    @Insert
    public void addData(SleepPeriod data);

    @Query("SELECT MAX(sleepDuration) FROM SleepPeriod")
    Float getMaxSleep();

    @Query("SELECT MIN(sleepDuration) FROM SleepPeriod")
    Float getMinSleep();

    @Query("SELECT * FROM SleepPeriod ORDER BY startTime DESC LIMIT 1")
    SleepPeriod getMostRecentSleepPeriod();

    @Query("SELECT * FROM SleepPeriod WHERE Date BETWEEN :startweek AND :endweek")
    List<SleepPeriod> getAllSleepPeriodWeek(long startweek, long endweek);


}
