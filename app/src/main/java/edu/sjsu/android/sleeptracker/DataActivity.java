package edu.sjsu.android.sleeptracker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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
    private static final String KEY_SAMPLE_DATA_ADDED = "sample_data_added";
    private SleepPeriodDatabase sleepPeriodDB;
    private BarChartView barChart;
    private TextView avgWeeklyText, avgOverallText, weekDisplayText;


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
        Button addSampleDataButton = findViewById(R.id.add_sample_data_button);
        weekDisplayText = findViewById(R.id.weekDisplay);

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

        boolean isSampleDataAdded = preferences.getBoolean(KEY_SAMPLE_DATA_ADDED, false);

        // Hide the button if sample data is already added
        if (isSampleDataAdded)
        {
            addSampleDataButton.setVisibility(View.GONE);
        }
        else
        {
            addSampleDataButton.setVisibility(View.VISIBLE);
            addSampleDataButton.setOnClickListener(v -> {
                addSampleData();
                // Save the flag to SharedPreferences
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(KEY_SAMPLE_DATA_ADDED, true);
                editor.apply();

                // Hide the button after adding data
                addSampleDataButton.setVisibility(View.GONE);
                recreate();
            });

        }

        loadDataForCurrentWeek();
    }

    private void addSampleData() {
        new Thread(() -> {
            try {
                long[][] data = {
                        {1733103000000L, 8, 1733082000000L, 1733119200000L},
                        {1733016600000L, 9, 1732995600000L, 1733032800000L},
                        {1732930200000L, 8, 1732909200000L, 1732946400000L},
                        {1732843800000L, 7, 1732822800000L, 1732863600000L},
                        {1732757400000L, 8, 1732736400000L, 1732780800000L},
                        {1732671000000L, (long) 7.5, 1732650000000L, 1732692000000L},
                        {1732584600000L, 9, 1732563600000L, 1732609200000L},
                        {1732498200000L, 8, 1732477200000L, 1732524000000L},
                        {1732411800000L, (long) 8.5f, 1732390800000L, 1732437600000L},
                        {1732325400000L, 7, 1732304400000L, 1732341600000L},
                        {1732239000000L, (long) 9.5f, 1732218000000L, 1732266000000L},
                        {1732152600000L, 8, 1732131600000L, 1732178400000L},
                        {1732066200000L, 8, 1732045200000L, 1732092000000L},
                        {1731979800000L, (long) 7.5f, 1731958800000L, 1732005600000L},
                        {1731893400000L, 9, 1731872400000L, 1731919200000L},
                        {1731807000000L, 8, 1731786000000L, 1731832800000L},
                        {1731720600000L, 8, 1731699600000L, 1731746400000L},
                        {1731634200000L, 7, 1731613200000L, 1731657600000L},
                        {1731547800000L, 9, 1731526800000L, 1731573600000L},
                        {1731461400000L, (long) 8.5f, 1731440400000L, 1731487200000L},
                        {1731375000000L, 7, 1731354000000L, 1731391200000L},
                        {1731288600000L, 9, 1731267600000L, 1731314400000L},
                        {1731202200000L, 8, 1731181200000L, 1731228000000L},
                        {1731115800000L, (long) 7.5f, 1731094800000L, 1731141600000L},
                        {1731029400000L, 8, 1731008400000L, 1731055200000L},
                        {1730943000000L, 8, 1730922000000L, 1730968800000L},
                        {1730856600000L, 9, 1730835600000L, 1730882400000L},
                        {1730770200000L, (long) 7.5f, 1730749200000L, 1730796000000L},
                        {1730683800000L, 8, 1730662800000L, 1730709600000L},
                        {1730597400000L, 9, 1730576400000L, 1730623200000L},
                        {1730511000000L, 7, 1730490000000L, 1730536800000L}
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

        calendar.add(Calendar.DAY_OF_WEEK, 7);
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
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd");
        String weekRange = "Week of " + dateFormat.format(startOfWeek) + "-" + dateFormat.format(endOfWeek);
        weekDisplayText.setText(weekRange);


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
