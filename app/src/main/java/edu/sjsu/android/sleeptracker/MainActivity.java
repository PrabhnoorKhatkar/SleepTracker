package edu.sjsu.android.sleeptracker;

import static edu.sjsu.android.sleeptracker.Converters.timestampToLong;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TimePicker;

import java.util.Date;
import java.util.List;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Timestamp;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MainActivity extends AppCompatActivity {

    private SleepDatabase sleepDB;
    private SleepPeriodDatabase sleepPeriodDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Intent serviceIntent = new Intent(this, SensorForegroundService.class);
        startForegroundService(serviceIntent);

        initTimePicker();

        Button dataButton = findViewById(R.id.data_button);
        dataButton.setOnClickListener(v -> {
            Intent dataIntent = new Intent(MainActivity.this, DataActivity.class);
            startActivity(dataIntent);
        });

    }

    private void initTimePicker()
    {
        saveSleepData();
        sleepPeriodDB = SleepPeriodDatabase.getInstance(getApplicationContext());
        TimePicker startTimePicker = findViewById(R.id.datePicker1);
        TimePicker endTimePicker = findViewById(R.id.datePicker2);

        new Thread(() -> {
            try {
                long last24Hours = System.currentTimeMillis() - 86400000;
                SleepPeriod mostRecentSleepPeriod = sleepPeriodDB.sleepPeriodDAO().getMostRecentSleepPeriod(last24Hours);

                if (mostRecentSleepPeriod != null)
                {
                    // Extract start and end time
                    Timestamp startTime = mostRecentSleepPeriod.getStartTime();
                    Timestamp endTime = mostRecentSleepPeriod.getEndTime();

                    // Convert Timestamp to Date
                    Date startDate = new Date(startTime.getTime());
                    Date endDate = new Date(endTime.getTime());

                    // Extract hours and minutes from Date
                    int startHour = startDate.getHours();
                    int startMinute = startDate.getMinutes();

                    int endHour = endDate.getHours();
                    int endMinute = endDate.getMinutes();


                    new Thread(() -> {
                        try
                        {
                            // Set the time on the time pickers
                            startTimePicker.setHour(startHour);
                            startTimePicker.setMinute(startMinute);
                            endTimePicker.setHour(endHour);
                            endTimePicker.setMinute(endMinute);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();

                }
                else
                {
                    Log.d("HI", "NO SLEEP DATA FOUND LAST 24 HOURS");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

    private void saveSleepData() {
        sleepDB = SleepDatabase.getInstance(getApplicationContext());

        // Retrieve sleep data
        try {
            new Thread(() -> {
                try {
                    List<SleepData> sleepDataList = sleepDB.sleepDataDAO().getAllSleepData();
                    processSleepData(sleepDataList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processSleepData(List<SleepData> sleepDataList) {
        sleepPeriodDB = SleepPeriodDatabase.getInstance(getApplicationContext());
        boolean isCurrentlySleeping = false;
        Timestamp sleepStartTime = null;

        for (SleepData currentData : sleepDataList) {
            if (currentData.getLux() < 50 && currentData.isCharging() && !isCurrentlySleeping) {
                // Start of a new sleep period
                isCurrentlySleeping = true;
                sleepStartTime = currentData.getTimestamp(); // Mark sleep start time
            }

            if (isCurrentlySleeping && !currentData.isCharging()) {
                // End of a sleep period
                isCurrentlySleeping = false;
                Timestamp sleepEndTime = currentData.getTimestamp(); // Mark sleep end time
                SleepPeriod sleepPeriod = new SleepPeriod(sleepStartTime, timestampToLong(sleepEndTime) - timestampToLong(sleepStartTime), sleepStartTime, sleepEndTime);

                new Thread(() -> {
                    try {
                        sleepPeriodDB.sleepPeriodDAO().addData(sleepPeriod);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        }
        sleepPeriodDB.closeDatabase();
    }
}
