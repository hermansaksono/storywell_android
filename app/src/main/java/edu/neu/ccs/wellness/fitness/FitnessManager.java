package edu.neu.ccs.wellness.fitness;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import edu.neu.ccs.wellness.fitness.challenges.ChallengeManager;
import edu.neu.ccs.wellness.fitness.interfaces.ChallengeManagerInterface;
import edu.neu.ccs.wellness.fitness.interfaces.FitnessManagerInterface;
import edu.neu.ccs.wellness.fitness.interfaces.GroupFitnessInterface;
import edu.neu.ccs.wellness.fitness.interfaces.OneDayFitnessInterface;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.sync.SyncData;

/**
 * Created by hermansaksono on 6/21/17.
 */

public class FitnessManager implements FitnessManagerInterface {

    // PRIVATE VARIABLES
    private static final String REST_RESOURCE = "group/activities/7d/";
    private static final String FILENAME = "FitnessManager.json";
    private static final int FIFTEEN_MINUTES = 900000;
    private RestServer server;
    private Context context;
    private SharedPreferences myPreferences;
    private JSONObject jsonObject;
    private SyncData syncData;

    /* CONSTRUCTOR */
    private FitnessManager(RestServer server, Context context) {
        this.server = server;
        this.context = context;
        this.syncData = new SyncData(server, context);
    }

    public static FitnessManagerInterface create(RestServer server, Context context){
        return new FitnessManager(server, context);
    }

    /* INTERFACE METHODS */
    @Override
    public GroupFitnessInterface getMultiDayFitness(Date startDate, Date endDate, Date cacheExpiryDate) {
        Date date = new Date();
        if(date.after(cacheExpiryDate)){
            //RK: taking a long time for the newtork call so using a background thread
            MyThread myThread = new MyThread();
            myThread.start();
            try {
                //RK: Due to background thread, need to wait before returning a value
                myThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            saveNewCacheExpiryDate(cacheExpiryDate);
        }else{
            jsonObject = syncData.requestJson(this.context, true, FILENAME, REST_RESOURCE);
        }

        return makeGroupFitness(jsonObject, startDate, endDate);
    }

    class MyThread extends Thread {

        //RK: Date is hardcoded as of now
        final String resource = REST_RESOURCE+"2017-06-01";

        MyThread(){
        }

        @Override
        public void run() {
            jsonObject = syncData.requestJson(context, false, FILENAME, resource);
        }
    }

    private GroupFitnessInterface makeGroupFitness(JSONObject jsonObject, Date startDate, Date endDate){
        JSONArray jsonArray = null;
        HashMap<Person, MultiDayFitness> personMultiDayFitnessMap = new HashMap<>();
        try {
            jsonArray = jsonObject.getJSONArray("activities");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        for(int i = 0; i<jsonArray.length(); i++){
            try {
                JSONObject eachGroupMemberJson = (JSONObject) jsonArray.get(i);
                Person person = makePerson(eachGroupMemberJson);
                MultiDayFitness multiDayFitness = makeMultiDayFitness(eachGroupMemberJson, startDate, endDate);
                personMultiDayFitnessMap.put(person, multiDayFitness);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return GroupFitness.create(context, personMultiDayFitnessMap);
    }

    private MultiDayFitness makeMultiDayFitness(JSONObject eachGroupMemberJson, Date startDate, Date endDate){
        JSONArray jsonArray = null;
        ArrayList<OneDayFitnessInterface> oneDayFitnesses = new ArrayList<>();
        Date sDate = startDate;
        Date eDate = endDate;
        int numberOfDays = 7;
        int elapsedDays = 0;
        try {
            jsonArray = eachGroupMemberJson.getJSONArray("activities");
            elapsedDays = jsonArray.length();
            for (int i = 0; i<elapsedDays; i++){
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                OneDayFitness oneDayFitness = null;
                try {
                    oneDayFitness = makeOneDayFitness(jsonObject);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                oneDayFitnesses.add(oneDayFitness);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return MultiDayFitness.create(context, sDate, eDate, numberOfDays, elapsedDays, oneDayFitnesses);
    }

    private OneDayFitness makeOneDayFitness(JSONObject jsonObject) throws JSONException, ParseException {
        DateFormat d = new SimpleDateFormat("yyyy-mm-dd");
        Date date = d.parse(jsonObject.getString("date"));
        int steps = jsonObject.getInt("steps");
       //TODO RK BigDecimal.valueOf(stringValue);
        float calories = jsonObject.getLong("calories");
        float distance = jsonObject.getLong("distance");
        float activeMinutes = TimeUnit.MILLISECONDS.toMinutes(date.getTime());
        return OneDayFitness.create(context, date, steps, calories, distance, activeMinutes);
    }

    private Person makePerson(JSONObject jsonObject){
        try {
            int id = jsonObject.getInt("id");
            String name = jsonObject.getString("name");
            String role = jsonObject.getString("role");
            return Person.newInstance(id, name, role);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    private void saveNewCacheExpiryDate(Date cacheExpiryDate){
        cacheExpiryDate.setTime(cacheExpiryDate.getTime() + FIFTEEN_MINUTES);
        myPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editPref = myPreferences.edit();
        editPref.putString("cacheExpiryDate", cacheExpiryDate.toString());
        editPref.apply();
    }
}
