package edu.neu.ccs.wellness.storytelling.parsers;

/**
 * Created by lilianngweta on 6/28/17.
 */


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.storytelling.models.Activities;

public class ActivitiesJSONParser {

    public static int[] parseIds(String content){

        try {
            //JSONArray ar = new JSONArray(content);

            JSONObject json = new JSONObject(content);
            JSONArray ar = json.getJSONArray("activities");
            List<Activities> idsList = new ArrayList<>();
            int[] steps = new int[20];

            for(int i=0; i<ar.length(); i++){

                JSONObject jsonPersonActivities = ar.getJSONObject(i);
                JSONArray jsonActivities = jsonPersonActivities.getJSONArray("activities");

                int personId = jsonPersonActivities.getInt("id");
//                int[] steps = new int[jsonActivities.length()];

                for (int j=0; j<jsonActivities.length(); i++) {
                    JSONObject jsonActivityOneDay = jsonActivities.getJSONObject(j);
                    steps[j] = jsonActivityOneDay.getInt("steps");
                }
//
//                Activities activities = new Activities();
//                activities.setId(jsonPersonActivities.getInt("id"));
//                idsList.add(activities);
            }

            return steps;

//            return idsList;
        } catch (JSONException e) {
            e.printStackTrace();
            return  null;
        }
    }



    public static List<Activities> parseSteps(String content){

        try {
            //JSONArray ar = new JSONArray(content);

            JSONObject json = new JSONObject(content);
            JSONArray ar = json.getJSONArray("activities");
            List<Activities> stepsList = new ArrayList<>();

            for(int i=0; i<ar.length(); i++){

                JSONArray jsonArray = ar.getJSONObject(i).getJSONArray("activities");
                Activities activities = new Activities();
                int[] steps = new int[7];
                String[] dates = new String[7];
                for(int j=0; j<7; j++){

                   JSONObject obj = jsonArray.getJSONObject(j);
                    steps[j] = obj.getInt("steps");
                    dates[j] = obj.getString("date");

                }

                activities.setSteps(steps);
                activities.setDate(dates);

                //activities.setId(obj.getInt("id"));
//                activities.setName(obj.getString("name"));
//                activities.setDate(obj.getString("date"));
//                activities.setRole(obj.getString("role"));
//                activities.setSteps(obj.getInt("steps"));
//                  activities.setName(obj.getString("name"));
                // activities.setValue(obj.getString("value"));

                stepsList.add(activities);


            }

            return stepsList;
        } catch (JSONException e) {
            e.printStackTrace();
            return  null;
        }
    }




}
