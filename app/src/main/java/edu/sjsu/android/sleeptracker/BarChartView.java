package edu.sjsu.android.sleeptracker;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import androidx.core.content.ContextCompat;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.util.ArrayList;
import java.util.List;

public class BarChartView extends BarChart {

    private SleepPeriodDatabase sleepPeriodDB;

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initChart(context);
    }

    private void initChart(Context context) {

        new Thread(() -> {
            try {

                sleepPeriodDB = SleepPeriodDatabase.getInstance(context.getApplicationContext());

                if(sleepPeriodDB.sleepPeriodDAO().getMinSleep() < 4){
                    getAxisLeft().setAxisMinimum(0f);
                }
                else {
                    getAxisLeft().setAxisMinimum(sleepPeriodDB.sleepPeriodDAO().getMinSleep() - 4);
                }
                getAxisLeft().setAxisMaximum(sleepPeriodDB.sleepPeriodDAO().getMaxSleep() + 4);

            } catch (Exception e)
            {
                e.printStackTrace();
            }
        }).start();


        XAxis xAxis = getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getDayLabels()));
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        getLegend().setEnabled(false);

        getAxisLeft().setGranularity(1f);
        getAxisRight().setEnabled(false);

        setDrawGridBackground(false);
        setDrawBarShadow(false);
        setPinchZoom(false);
        setDrawValueAboveBar(true);

        setChartColors(context);
    }

    private List<String> getDayLabels() {
        List<String> dayLabels = new ArrayList<>();
        dayLabels.add("Sun");
        dayLabels.add("Mon");
        dayLabels.add("Tue");
        dayLabels.add("Wed");
        dayLabels.add("Thu");
        dayLabels.add("Fri");
        dayLabels.add("Sat");
        return dayLabels;
    }

    public void setData(List<Float> data) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            entries.add(new BarEntry(i, data.get(i)));
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        applyDataSetColors(dataSet);
        BarData barData = new BarData(dataSet);

        super.setData(barData);
        invalidate();
    }

    private void setChartColors(Context context) {
        boolean isDarkMode = (context.getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        int axisTextColor = isDarkMode ?
                ContextCompat.getColor(context, R.color.on_background_dark) :
                ContextCompat.getColor(context, R.color.on_background_light);

        int gridLineColor = isDarkMode ?
                ContextCompat.getColor(context, R.color.grid_dark) :
                ContextCompat.getColor(context, R.color.grid_light);

        getXAxis().setTextColor(axisTextColor);
        getXAxis().setGridColor(gridLineColor);

        getAxisLeft().setTextColor(axisTextColor);
        getAxisLeft().setGridColor(gridLineColor);

        getAxisRight().setTextColor(axisTextColor);
        getAxisRight().setGridColor(gridLineColor);
    }

    private void applyDataSetColors(BarDataSet dataSet) {
        boolean isDarkMode = (getResources().getConfiguration().uiMode &
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;

        int barColor = isDarkMode ?
                ContextCompat.getColor(getContext(), R.color.primary_dark) :
                ContextCompat.getColor(getContext(), R.color.primary_light);

        dataSet.setColor(barColor);
        dataSet.setValueTextColor(barColor);
    }
}
