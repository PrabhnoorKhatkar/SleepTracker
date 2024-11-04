package edu.sjsu.android.sleeptracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataActivity extends AppCompatActivity {

    private long startOfWeek;
    private long endOfWeek;
    private SleepPeriodDatabase sleepPeriodDB;
    private BarChart barChart;
    private TextView avgWeeklyText, avgOverallText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        Button homeButton = findViewById(R.id.home_button);
        ImageButton previousWeekButton = findViewById(R.id.previous_week_button);
        ImageButton nextWeekButton = findViewById(R.id.next_week_button);
        barChart = findViewById(R.id.bar_chart_view);
        avgWeeklyText = findViewById(R.id.average_weekly);
        avgOverallText = findViewById(R.id.average_overall);

        sleepPeriodDB = SleepPeriodDatabase.getInstance(getApplicationContext());

        updateWeekRange(System.currentTimeMillis());

        homeButton.setOnClickListener(v -> navigateBackHome());
        previousWeekButton.setOnClickListener(v -> showPreviousWeek());
        nextWeekButton.setOnClickListener(v -> showNextWeek());

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
        loadDataForCurrentWeek();
    }

    private void showNextWeek() {

        startOfWeek += 7 * 86400000L;
        endOfWeek += 7 * 86400000L;
        loadDataForCurrentWeek();
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
        List<BarEntry> barEntries = new ArrayList<>();

        for (int i = 0; i < sleepCount; i++) {
            SleepPeriod period = sleepPeriods.get(i);
            float duration = period.getDuration();
            totalSleep += duration;
            barEntries.add(new BarEntry(i, duration));
            Log.d("DataActivity", "SleepPeriod: " + period.toString());
        }

        float avgWeekly = sleepCount > 0 ? totalSleep / 7 : 0;
        float avgOverall = sleepCount > 0 ? totalSleep / sleepCount : 0;

        avgWeeklyText.setText("Average Weekly: " + avgWeekly + " hours");
        avgOverallText.setText("Average Overall: " + avgOverall + " hours");

        BarDataSet dataSet = new BarDataSet(barEntries, "Sleep Duration (Hours)");
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.invalidate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigateBackHome();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
