package edu.sjsu.android.sleeptracker;

import androidx.room.TypeConverter;

import java.sql.Timestamp;

// Adapted from https://developer.android.com/training/data-storage/room/referencing-data
public class Converters
{
    
    @TypeConverter
    public static Long timestampToLong(Timestamp timestamp)
    {
        return timestamp == null ? null : timestamp.getTime();
    }

}