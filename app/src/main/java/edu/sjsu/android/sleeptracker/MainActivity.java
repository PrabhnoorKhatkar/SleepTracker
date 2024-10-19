package edu.sjsu.android.sleeptracker;

import static edu.sjsu.android.sleeptracker.Converters.timestampToLong;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import java.util.List;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Timestamp;

public class MainActivity extends AppCompatActivity {

    private SleepDatabase sleepDB;
    private SleepPeriodDatabase sleepPeriodDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button saveButton = findViewById(R.id.Save);
        saveButton.setOnClickListener(v -> {
            saveSleepData();
        });

        Button dataButton = findViewById(R.id.data_button);
        dataButton.setOnClickListener(v -> {
            Intent dataIntent = new Intent(MainActivity.this, DataActivity.class);
            startActivity(dataIntent);
        });
    }

    private void saveSleepData() {
        sleepDB = SleepDatabase.getInstance(getApplicationContext());

        // THIS LINE BREAKS CODE
        // Retrieve sleep data
        try {
            List<SleepData> sleepDataList = sleepDB.sleepDataDAO().getAllSleepData();
            processSleepData(sleepDataList);
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
                SleepPeriod sleepPeriod = new SleepPeriod(sleepStartTime,
                        timestampToLong(sleepEndTime) - timestampToLong(sleepStartTime), sleepStartTime, sleepEndTime);

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
