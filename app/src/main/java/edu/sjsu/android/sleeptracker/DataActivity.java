package edu.sjsu.android.sleeptracker;

import static java.lang.System.currentTimeMillis;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
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

        barChart = findViewById(R.id.bar_chart_view);
        avgWeeklyText = findViewById(R.id.average_weekly);
        avgOverallText = findViewById(R.id.average_overall);
        sleepPeriodDB = SleepPeriodDatabase.getInstance(getApplicationContext());

        new Thread(() -> {
            try {
                Date startTime = Date.from(Instant.now().minusSeconds(3600));  // 1 hour ago
                Date endTime = Date.from(Instant.now());
                float duration = 7.5f;
                Timestamp date = new Timestamp(System.currentTimeMillis());
                SleepPeriod mockSleepPeriod = new SleepPeriod(date, duration, new Timestamp(startTime.getTime()), new Timestamp(endTime.getTime()));
                sleepPeriodDB.sleepPeriodDAO().addData(mockSleepPeriod);
                Log.d("DataActivity", "Mock sleep data added successfully.");
            } catch (Exception e) {
                Log.e("DataActivity", "Error inserting mock sleep data", e);
            }
        }).start();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            List<SleepPeriod> sleepPeriods = sleepPeriodDB.sleepPeriodDAO().getAllSleepPeriodData();
            runOnUiThread(() -> displayData(sleepPeriods));
        });
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

        // Update the text views with averages
        avgWeeklyText.setText("Average Weekly: " + avgWeekly + " hours");
        avgOverallText.setText("Average Overall: " + avgOverall + " hours");

        BarDataSet dataSet = new BarDataSet(barEntries, "Sleep Duration (Hours)");
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.invalidate();  // Refresh the chart
    }


}
