package edu.sjsu.android.sleeptracker;

import static edu.sjsu.android.sleeptracker.Converters.timestampToLong;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;
import java.util.Calendar;
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

    private static final String PREFS_NAME = "TimePickerPref";
    private static final String KEY_START_HOUR = "startHour";
    private static final String KEY_START_MINUTE = "startMinute";
    private static final String KEY_END_HOUR = "endHour";
    private static final String KEY_END_MINUTE = "endMinute";



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

        Button saveButton = findViewById(R.id.Save);
        saveButton.setOnClickListener(v -> saveButton());

        Button dataButton = findViewById(R.id.data_button);
        dataButton.setOnClickListener(v -> {
            Intent dataIntent = new Intent(MainActivity.this, DataActivity.class);
            startActivity(dataIntent);
        });

    }

    private void saveButton() {
        TimePicker startTimePicker = findViewById(R.id.datePicker1);
        TimePicker endTimePicker = findViewById(R.id.datePicker2);

        // Use the morning/evening of to count for that sleep
        // Retrieve TimePicker values
        int startHour = startTimePicker.getHour();
        int startMinute = startTimePicker.getMinute();
        int endHour = endTimePicker.getHour();
        int endMinute = endTimePicker.getMinute();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfDayTimestamp = calendar.getTimeInMillis();

        // Determine AM/PM for endTime
        boolean endIsAM = endHour < 12;
        // Determine AM/PM for startTime
        boolean startIsAM = startHour < 12;

        // Calculate the total duration of sleep
        int sleepDurationHours = 0;
        // TODO round up or down on sleep minutes
        int sleepDurationMinutes = 0;

        if (endIsAM) {
            if (startIsAM) {
                // Both start and end are in AM on same day
                if (endHour > startHour && endMinute > startMinute) {
                    sleepDurationHours = endHour - startHour;
                    sleepDurationMinutes = endMinute - startMinute;

                } else {
                    // eg. 11 AM => 4 AM
                    // Start in AM and sleep for more than 12 hours into next day AM
                    sleepDurationHours = (24 - startHour) + endHour;
                    sleepDurationMinutes = endMinute - startMinute;
                }
            } else {
                // eg 11 pm => 8am
                // Start is PM, End is AM (normal sleep)
                sleepDurationHours = (24 - startHour) + endHour;
                sleepDurationMinutes = endMinute - startMinute;
            }
        } else // end is PM
        {
            // eg 3 AM => 4 PM
            if (startIsAM) {
                // Start is AM
                sleepDurationHours = endHour - startHour;
                sleepDurationMinutes = endMinute - startMinute;
            } else {
                if (startHour > endHour) {
                    // eg 11PM => 1 PM
                    // Start is PM but sleep to next day PM
                    sleepDurationHours = (24 - startHour) + endHour;
                    sleepDurationMinutes = endMinute - startMinute;

                } else {
                    // eg 1PM => 4 PM
                    // Start is PM but sleep to next day PM
                    sleepDurationHours = endHour - startHour;
                    sleepDurationMinutes = endMinute - startMinute;

                }

            }
        }

        // Create SleepPeriod and save to database
        SleepPeriod sleepPeriod = new SleepPeriod(new Timestamp(startOfDayTimestamp), sleepDurationHours, new Timestamp(0L), new Timestamp(0L));
        int finalSleepDurationHours = sleepDurationHours;
        new Thread(() -> {
            try {
                SleepPeriod existing = sleepPeriodDB.sleepPeriodDAO().getSleepPeriodByDate(sleepPeriod.getDate().getTime());
                if (existing == null) {
                    sleepPeriodDB.sleepPeriodDAO().addData(sleepPeriod);
                    runOnUiThread(() ->
                            Toast.makeText(this, "Sleep Period Added", Toast.LENGTH_SHORT).show()
                    );
                } else {
                    existing.setDuration(finalSleepDurationHours);
                    sleepPeriodDB.sleepPeriodDAO().updateData(existing);
                    runOnUiThread(() ->
                            Toast.makeText(this, "Sleep Period Updated", Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

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


    private void initTimePicker() {
        sleepPeriodDB = SleepPeriodDatabase.getInstance(getApplicationContext());

        TimePicker startTimePicker = findViewById(R.id.datePicker1);
        TimePicker endTimePicker = findViewById(R.id.datePicker2);


        new Thread(() -> {
            try {
                SleepPeriod mostRecentSleepPeriod = sleepPeriodDB.sleepPeriodDAO().getMostRecentSleepPeriod();

                if (mostRecentSleepPeriod != null) {
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
                        try {
                            // Set the time on the time pickers
                            startTimePicker.setHour(endHour);
                            startTimePicker.setMinute(endMinute);
                            endTimePicker.setHour(startHour);
                            endTimePicker.setMinute(startMinute);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();

                } else {
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

    @Override
    protected void onPause() {
        super.onPause();

        TimePicker startTimePicker = findViewById(R.id.datePicker1);
        TimePicker endTimePicker = findViewById(R.id.datePicker2);

        // Retrieve TimePicker values
        int startHour = startTimePicker.getHour();
        int startMinute = startTimePicker.getMinute();
        int endHour = endTimePicker.getHour();
        int endMinute = endTimePicker.getMinute();

        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt(KEY_START_HOUR, startHour);
        editor.putInt(KEY_START_MINUTE, startMinute);
        editor.putInt(KEY_END_HOUR, endHour);
        editor.putInt(KEY_END_MINUTE, endMinute);
        editor.apply();

    }

    @Override
    protected void onResume() {
        super.onResume();

        TimePicker startTimePicker = findViewById(R.id.datePicker1);
        TimePicker endTimePicker = findViewById(R.id.datePicker2);

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        // Set the saved hour and minute values
        int startHour = preferences.getInt(KEY_START_HOUR, 0);
        int startMinute = preferences.getInt(KEY_START_MINUTE, 0);
        int endHour = preferences.getInt(KEY_END_HOUR, 0);
        int endMinute = preferences.getInt(KEY_END_MINUTE, 0);

        // Set the restored values to the TimePickers
        startTimePicker.setHour(startHour);
        startTimePicker.setMinute(startMinute);
        endTimePicker.setHour(endHour);
        endTimePicker.setMinute(endMinute);


    }
}
