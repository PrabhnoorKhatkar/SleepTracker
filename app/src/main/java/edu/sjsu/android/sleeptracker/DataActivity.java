package edu.sjsu.android.sleeptracker;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class DataActivity extends AppCompatActivity {

    private SleepPeriodDatabase sleepPeriodDB;
    private BarChartView barChartView;
    private TextView avgWeeklyText, avgOverallText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        barChartView = findViewById(R.id.bar_chart_view);
        avgWeeklyText = findViewById(R.id.average_weekly);
        avgOverallText = findViewById(R.id.average_overall);

        sleepPeriodDB = SleepPeriodDatabase.getInstance(getApplicationContext());
        List<SleepPeriod> sleepPeriods = sleepPeriodDB.sleepPeriodDAO().getAllSleepPeriodData();

        displayData(sleepPeriods);
    }

    private void displayData(List<SleepPeriod> sleepPeriods) {

        float totalSleep = 0;
        int sleepCount = sleepPeriods.size();
        List<Integer> sleepDurations = new ArrayList<>();

        for (int i = 0; i < sleepCount; i++) {
            SleepPeriod period = sleepPeriods.get(i);
            totalSleep += period.getDuration();
            sleepDurations.add((int) period.getDuration());
        }

        float avgWeekly = totalSleep / 7;
        float avgOverall = totalSleep / sleepCount;

        avgWeeklyText.setText("Average Weekly: " + avgWeekly + " hours");
        avgOverallText.setText("Average Overall: " + avgOverall + " hours");

        barChartView.setData(sleepDurations);
    }
}
