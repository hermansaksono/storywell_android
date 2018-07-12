package edu.neu.ccs.wellness.storytelling;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.fitness.unused.Activities;
//import edu.neu.ccs.wellness.fitness.ActivitiesManager;
import edu.neu.ccs.wellness.server.WellnessRestServer;
import edu.neu.ccs.wellness.server.WellnessUser;
import edu.neu.ccs.wellness.fitness.unused.ActivitiesJSONParser;


public class ActivitiesFragment extends Fragment {

    public static final String WELLNESS_SERVER_URL = "http://wellness.ccs.neu.edu/";
    public static final String ACTIVITIES_API_PATH = "storytelling_dev/api/";

   // private ActivitiesManager activitiesManager;
    private WellnessUser user;
    private WellnessRestServer server;

    private View sitem;
    private TextView tv;
    ListView list;
    View rootView;

    //TextView output;
    List<Activities> activitiesList;
    ArrayList<String> days;
    BarChart barChart;
    BarChart barChart1;
    Activities activities;
    ArrayList<BarEntry> barEntries;
    ArrayList<BarEntry> barEntries1;

    public static ActivitiesFragment newInstance() {
        return new ActivitiesFragment();
    }


    //TODO Look into how to work with nested JSON
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_activities, container, false);

        barChart = (BarChart) rootView.findViewById(R.id.bargraph);
        barChart1 = (BarChart) rootView.findViewById(R.id.bargraph1);
        ReadLocalJson readLocalJson = new ReadLocalJson();
        readLocalJson.execute();
        return rootView;
    }

    private void updateDisplay() {

        barEntries = new ArrayList<>();
        barEntries1 = new ArrayList<>();

        days = new ArrayList<>();
//        days.add("Mon");
//        days.add("Tue");
//        days.add("Wed");
//        days.add("Thu");
//        days.add("Fri");
//        days.add("Sat");
//        days.add("Sun");

        int count = 0;
        activities = new Activities();

        if (activitiesList != null) {

            for (Activities activities : activitiesList) {
//                StringBuilder sb = new StringBuilder();

                while (count < 7) {
                    for (int step : activities.getSteps()) {
//                    sb.append(step);
//                    sb.append(" ");

                        barEntries.add(new BarEntry(count, step));
                        barEntries1.add(new BarEntry(count, step));
                        count++;
                    }
//                output.append(sb.toString() + "\n");


                    for (String date : activities.getDate()) {
                        days.add(date);
                    }
                }
            }
        }

        BarDataSet steps = new BarDataSet(barEntries, "Steps");
        BarDataSet steps1 = new BarDataSet(barEntries1, "Steps");

        BarData theData = new BarData(steps);
        BarData theData1 = new BarData(steps1);


        IAxisValueFormatter formatter = new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return days.get((int) value);
            }

        };

        XAxis xAxis = barChart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);


        IAxisValueFormatter formatter1 = new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return days.get((int) value);
            }

        };

        XAxis xAxis1 = barChart1.getXAxis();
        xAxis1.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis1.setValueFormatter(formatter1);


        barChart.setData(theData);
        barChart1.setData(theData1);

        barChart.setTouchEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setScaleEnabled(true);


        barChart1.setTouchEnabled(true);
        barChart1.setDragEnabled(true);
        barChart1.setScaleEnabled(true);

    }
    // PRIVATE METHODS

    // PRIVATE ASYNCTASK CLASSES

    private class ReadLocalJson extends AsyncTask<String, String, String> {

        ArrayList<String> value_array = new ArrayList<>();

        @Override
        protected String doInBackground(String... params) {

            String json = null;
            InputStream is = getResources().openRawResource(R.raw.activities);

            try {
                int size = is.available();

                byte[] buffer = new byte[size];

                is.read(buffer);

                json = new String(buffer, "UTF-8");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return json;

        }


        @Override
        protected void onPostExecute(String result) {
            activitiesList = ActivitiesJSONParser.parseSteps(result);
            updateDisplay();
        }

    }
}