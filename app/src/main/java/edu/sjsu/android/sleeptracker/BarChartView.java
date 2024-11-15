package edu.sjsu.android.sleeptracker;

import android.content.Context;
import android.util.AttributeSet;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.util.ArrayList;
import java.util.List;

public class BarChartView extends BarChart {

    public BarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initChart();
    }

    private void initChart() {
        XAxis xAxis = getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getDayLabels()));
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        getLegend().setEnabled(false);

        getAxisLeft().setAxisMinimum(7f);
        getAxisLeft().setAxisMaximum(12f);
        getAxisLeft().setGranularity(1f);
        getAxisRight().setEnabled(false);

        setDrawGridBackground(false);
        setDrawBarShadow(false);
        setPinchZoom(false);
        setDrawValueAboveBar(true);
    }

    private List<String> getDayLabels() {
        List<String> dayLabels = new ArrayList<>();
        dayLabels.add("Mon");
        dayLabels.add("Tue");
        dayLabels.add("Wed");
        dayLabels.add("Thu");
        dayLabels.add("Fri");
        dayLabels.add("Sat");
        dayLabels.add("Sun");
        return dayLabels;
    }

    public void setData(List<Float> data) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            entries.add(new BarEntry(i, data.get(i)));
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        BarData barData = new BarData(dataSet);
        super.setData(barData);
        invalidate();
    }

}
