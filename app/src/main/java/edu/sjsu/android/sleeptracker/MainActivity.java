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

        // Retrieve TimePicker values
        int startHour = startTimePicker.getHour();
        int startMinute = startTimePicker.getMinute();
        int endHour = endTimePicker.getHour();
        int endMinute = endTimePicker.getMinute();

        // Set calendar instances for precise calculations
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(Calendar.HOUR_OF_DAY, startHour);
        startCalendar.set(Calendar.MINUTE, startMinute);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 0);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.HOUR_OF_DAY, endHour);
        endCalendar.set(Calendar.MINUTE, endMinute);
        endCalendar.set(Calendar.SECOND, 0);
        endCalendar.set(Calendar.MILLISECOND, 0);

        // Handle cross-day scenarios
        if (endCalendar.getTimeInMillis() <= startCalendar.getTimeInMillis()) {
            endCalendar.add(Calendar.DATE, 1); // End time is on the next day
        }

        long durationInMillis = endCalendar.getTimeInMillis() - startCalendar.getTimeInMillis();
        int sleepDurationHours = (int) (durationInMillis / (1000 * 60 * 60));
        int sleepDurationMinutes = (int) ((durationInMillis % (1000 * 60 * 60)) / (1000 * 60));


        if(sleepDurationMinutes > 31)
        {
            sleepDurationHours++;
        }
        // Create SleepPeriod and save to database
        SleepPeriod sleepPeriod = new SleepPeriod(new Timestamp(startCalendar.getTimeInMillis()), sleepDurationHours, new Timestamp(0L), new Timestamp(0L));
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

                    // Extract hours and minutes from Calender
                    Calendar startCalendar = Calendar.getInstance();
                    startCalendar.setTime(startDate);

                    Calendar endCalendar = Calendar.getInstance();
                    endCalendar.setTime(endDate);

                    int startHour = startCalendar.get(Calendar.HOUR_OF_DAY);
                    int startMinute = startCalendar.get(Calendar.MINUTE);

                    int endHour = endCalendar.get(Calendar.HOUR_OF_DAY);
                    int endMinute = endCalendar.get(Calendar.MINUTE);


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
