package edu.sjsu.android.sleeptracker;

import static edu.sjsu.android.sleeptracker.Converters.timestampToLong;

import android.content.Context;
import java.util.List;
import android.content.Intent;
import android.os.Bundle;
import java.sql.Timestamp;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
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


        sleepDB = SleepDatabase.getInstance(getApplicationContext());
        sleepPeriodDB = SleepPeriodDatabase.getInstance(getApplicationContext());

        // THIS LINE BREAKS CODE
        // Retrieve sleep data
        List<SleepData> sleepDataList = sleepDB.sleepDataDAO().getAllSleepData();

        /*

        processSleepData(sleepDataList);
        */

    }

    private void processSleepData(List<SleepData> sleepDataList) {
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
                sleepPeriodDB.sleepPeriodDAO().addData(sleepPeriod);
            }
        }
    }
}





