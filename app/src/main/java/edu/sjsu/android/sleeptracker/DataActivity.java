package edu.sjsu.android.sleeptracker;

import android.os.Bundle;
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

        barChart = findViewById(R.id.bar_chart_view);
        avgWeeklyText = findViewById(R.id.average_weekly);
        avgOverallText = findViewById(R.id.average_overall);

        sleepPeriodDB = SleepPeriodDatabase.getInstance(getApplicationContext());

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {

            List<SleepPeriod> sleepPeriods = sleepPeriodDB.sleepPeriodDAO().getAllSleepPeriodData();

            runOnUiThread(() -> {
                displayData(sleepPeriods);
            });
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
        }
        float avgWeekly = totalSleep / 7;
        float avgOverall = totalSleep / sleepCount;

        // Update TextViews with the calculated averages
        avgWeeklyText.setText("Average Weekly: " + avgWeekly + " hours");
        avgOverallText.setText("Average Overall: " + avgOverall + " hours");

        BarDataSet dataSet = new BarDataSet(barEntries, "Sleep Duration (Hours)");
        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.invalidate();
    }
}
