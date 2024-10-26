package edu.sjsu.android.sleeptracker;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataActivity extends AppCompatActivity {

    private SleepPeriodDatabase sleepPeriodDB;
    private BarChart barChart;
    private TextView avgWeeklyText, avgOverallText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        Button homeButton = findViewById(R.id.home_button);
        homeButton.setOnClickListener(v -> navigateBackHome());

        barChart = findViewById(R.id.bar_chart_view);
        avgWeeklyText = findViewById(R.id.average_weekly);
        avgOverallText = findViewById(R.id.average_overall);
        sleepPeriodDB = SleepPeriodDatabase.getInstance(getApplicationContext());

        long startOfWeek = System.currentTimeMillis() - (7 * 86400000);
        long endOfWeek = System.currentTimeMillis();

        new Thread(() -> {
            try {
                List<SleepPeriod> sleepPeriods = sleepPeriodDB.sleepPeriodDAO().getAllSleepPeriodWeek(startOfWeek, endOfWeek);
                for (SleepPeriod period : sleepPeriods) {
                    Log.d("DataActivity", "Sleep Period: " + period.toString());
                }
            } catch (Exception e) {
                Log.e("DataActivity", "Error retrieving sleep data", e);
            }
        }).start();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<SleepPeriod> sleepPeriods = sleepPeriodDB.sleepPeriodDAO().getAllSleepPeriodWeek(startOfWeek, endOfWeek);
            runOnUiThread(() -> displayData(sleepPeriods));
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

        float avgWeekly = totalSleep / 7;
        float avgOverall = totalSleep / sleepCount;

        avgWeeklyText.setText("Average Weekly: " + avgWeekly + " hours");
        avgOverallText.setText("Average Overall: " + avgOverall + " hours");

        BarDataSet dataSet = new BarDataSet(barEntries, "Sleep Duration (Hours)");
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.invalidate();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Navigate back to the home screen
                navigateBackHome();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
