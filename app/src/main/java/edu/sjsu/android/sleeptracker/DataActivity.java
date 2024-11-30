package edu.sjsu.android.sleeptracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataActivity extends AppCompatActivity {

    private long startOfWeek;
    private long endOfWeek;
    private final String KEY_START_OF_WEEK = "start_of_week";
    private final String KEY_END_OF_WEEK = "end_of_week";
    private SleepPeriodDatabase sleepPeriodDB;
    private BarChartView barChart;
    private TextView avgWeeklyText, avgOverallText;


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
        if (item.getItemId() == android.R.id.home) {
            navigateBackHome();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeUtils.applyTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        Button homeButton = findViewById(R.id.home_button);
        ImageButton previousWeekButton = findViewById(R.id.previous_week_button);
        ImageButton nextWeekButton = findViewById(R.id.next_week_button);
        barChart = findViewById(R.id.bar_chart_view);
        avgWeeklyText = findViewById(R.id.average_weekly);
        avgOverallText = findViewById(R.id.average_overall);

        sleepPeriodDB = SleepPeriodDatabase.getInstance(getApplicationContext());

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        startOfWeek = preferences.getLong(KEY_START_OF_WEEK, 0);
        endOfWeek = preferences.getLong(KEY_END_OF_WEEK, 0);

        // If no values saved, init with the current week
        if (startOfWeek == 0 || endOfWeek == 0) {
            updateWeekRange(System.currentTimeMillis());
        }

        updateWeekRange(System.currentTimeMillis());

        homeButton.setOnClickListener(v -> navigateBackHome());
        previousWeekButton.setOnClickListener(v -> showPreviousWeek());
        nextWeekButton.setOnClickListener(v -> showNextWeek());

        loadDataForCurrentWeek();
    }

    private void addSampleData() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                // Open the sample data file from assets
                InputStream inputStream = getAssets().open("sample_data.csv");
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                List<SleepPeriod> sampleData = new ArrayList<>();

                // Read each line from the file
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split(",");
                    if (fields.length == 4) {
                        long date = Long.parseLong(fields[0].trim());
                        float duration = Float.parseFloat(fields[1].trim());
                        long startTime = Long.parseLong(fields[2].trim());
                        long endTime = Long.parseLong(fields[3].trim());

                        // Create a new SleepPeriod object
                        SleepPeriod sleepPeriod = new SleepPeriod( new Timestamp(date), duration, new Timestamp(startTime), new Timestamp(endTime));

                        sampleData.add(sleepPeriod);
                    }
                }

                // Add sample data to the database
                for (SleepPeriod sleepPeriod : sampleData) {
                    sleepPeriodDB.sleepPeriodDAO().addData(sleepPeriod);
                }

                runOnUiThread(this::loadDataForCurrentWeek);
            } catch (Exception e) {
                Log.e("DataActivity", "Error adding sample data", e);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Get saved week range from SharedPreferences
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        startOfWeek = preferences.getLong(KEY_START_OF_WEEK, 0);
        endOfWeek = preferences.getLong(KEY_END_OF_WEEK, 0);

        // If no values saved, init with the current week
        if (startOfWeek == 0 || endOfWeek == 0) {
            updateWeekRange(System.currentTimeMillis());
        }

        // Load data for the current week
        loadDataForCurrentWeek();
    }


    private void updateWeekRange(long referenceTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(referenceTime);

        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        startOfWeek = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_WEEK, 6);
        endOfWeek = calendar.getTimeInMillis();
    }

    private void showPreviousWeek() {
        startOfWeek -= 7 * 86400000L;
        endOfWeek -= 7 * 86400000L;
        saveWeekRange();
        loadDataForCurrentWeek();
    }

    private void showNextWeek() {
        startOfWeek += 7 * 86400000L;
        endOfWeek += 7 * 86400000L;
        saveWeekRange();
        loadDataForCurrentWeek();
    }

    private void saveWeekRange() {
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putLong(KEY_START_OF_WEEK, startOfWeek);
        editor.putLong(KEY_END_OF_WEEK, endOfWeek);
        editor.apply();
    }

    private void loadDataForCurrentWeek() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                List<SleepPeriod> sleepPeriods = sleepPeriodDB.sleepPeriodDAO().getAllSleepPeriodWeek(startOfWeek, endOfWeek);
                runOnUiThread(() -> displayData(sleepPeriods));
            } catch (Exception e) {
                Log.e("DataActivity", "Error retrieving sleep data", e);
            }
        });
    }

    private void navigateBackHome() {
        Intent intent = new Intent(DataActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void displayData(List<SleepPeriod> sleepPeriods) {
        float totalSleep = 0;
        int sleepCount = sleepPeriods.size();
        List<Float> durations = new ArrayList<>();

        for (SleepPeriod period : sleepPeriods) {
            float duration = period.getDuration();
            totalSleep += duration;
            durations.add(duration);
        }

        float avgWeekly = sleepCount > 0 ? totalSleep / 7 : 0;
        float avgOverall = sleepCount > 0 ? totalSleep / sleepCount : 0;

        avgWeeklyText.setText("Average Weekly: " + avgWeekly + " hours");
        avgOverallText.setText("Average Overall: " + avgOverall + " hours");

        barChart.setData(durations);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();
        editor.putLong(KEY_START_OF_WEEK, startOfWeek);
        editor.putLong(KEY_END_OF_WEEK, endOfWeek);
        editor.apply();
    }
}
