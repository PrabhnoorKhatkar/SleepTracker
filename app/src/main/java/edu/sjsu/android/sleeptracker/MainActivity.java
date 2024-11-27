package edu.sjsu.android.sleeptracker;

import static edu.sjsu.android.sleeptracker.Converters.timestampToLong;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
        ThemeUtils.applyTheme(this);
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

        saveSleepData();
        initTimePicker();

        Button dataButton = findViewById(R.id.data_button);
        dataButton.setOnClickListener(v -> {
            Intent dataIntent = new Intent(MainActivity.this, DataActivity.class);
            startActivity(dataIntent);
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_toggle_theme) {
            ThemeUtils.toggleDarkMode(this);
            recreate(); // Recreate the activity to apply the theme
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void initTimePicker()
    {
        sleepPeriodDB = SleepPeriodDatabase.getInstance(getApplicationContext());
        /*
        // Debugging/Testing Only
        new Thread(() -> {
            try {
                long[][] data = {
                        {1729852206000L, 10, 1729914000000L, 1729950000000L},
                        {1729938606000L, 9, 1729999200000L, 1730031600000L},
                        {1730025006000L, 10, 1730085600000L, 1730121600000L},
                        {1730111406000L, 11, 1730172000000L, 1730211600000L},
                        {1730197806000L, 9, 1730258400000L, 1730290800000L},
                        {1730284206000L, 8, 1730344800000L, 1730373600000L},
                        {1730370606000L, 8, 1730431200000L, 1730460000000L},
                        {1730457006000L, 8, 1730517600000L, 1730546400000L},
                        {1730543406000L, 10, 1730604000000L, 1730640000000L},
                        {1730629806000L, 9, 1730690400000L, 1730722800000L},
                        {1730716206000L, 12, 1730776800000L, 1730820000000L},
                        {1730802606000L, 9, 1730863200000L, 1730895600000L}
                };

                for (long[] entry : data) {
                    SleepPeriod addData = new SleepPeriod(
                            new Timestamp(entry[0]), // date
                            (float) entry[1],          // duration
                            new Timestamp(entry[2]), // starttime
                            new Timestamp(entry[3])  // endtime
                    );
                    sleepPeriodDB.sleepPeriodDAO().addData(addData);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

         */
        // TODO Remove when done

        TimePicker startTimePicker = findViewById(R.id.datePicker1);
        TimePicker endTimePicker = findViewById(R.id.datePicker2);

        new Thread(() -> {
            try {
                SleepPeriod mostRecentSleepPeriod = sleepPeriodDB.sleepPeriodDAO().getMostRecentSleepPeriod();

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
    }
}
