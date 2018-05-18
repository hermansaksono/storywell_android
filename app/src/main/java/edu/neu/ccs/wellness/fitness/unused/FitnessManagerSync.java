package edu.neu.ccs.wellness.fitness.unused;

import android.content.Context;
import android.content.SharedPreferences;
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

import edu.neu.ccs.wellness.fitness.GroupFitness;
import edu.neu.ccs.wellness.fitness.MultiDayFitness;
import edu.neu.ccs.wellness.fitness.OneDayFitness;
import edu.neu.ccs.wellness.fitness.interfaces.FitnessManagerInterface;
import edu.neu.ccs.wellness.fitness.interfaces.GroupFitnessInterface;
import edu.neu.ccs.wellness.fitness.interfaces.OneDayFitnessInterface;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.WellnessRepository;

/**
 * Created by hermansaksono on 6/21/17.
 */

public class FitnessManagerSync implements FitnessManagerInterface {

    // PRIVATE VARIABLES
    private static final String REST_RESOURCE = "group/activities/7d/";
    private static final String FILENAME = "FitnessManager.json";
    private static final int FIFTEEN_MINUTES = 900000;
    private RestServer server;
    private Context context;
    private SharedPreferences myPreferences;
    private JSONObject jsonObject;
    private WellnessRepository repository;

    /* CONSTRUCTOR */
    private FitnessManagerSync(RestServer server, Context context) {
        this.server = server;
        this.context = context;
        this.repository = new WellnessRepository(server, context);
    }

    public static FitnessManagerInterface create(RestServer server, Context context){
        return new FitnessManagerSync(server, context);
    }

    /* INTERFACE METHODS */
    // TODO HS: I like the direction you are going, but we can just indicate in the method's purpose
    // TODO   : statement that this method should be called within AsyncTask or AsyncLoader
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
            jsonObject = repository.requestJson(this.context, true, FILENAME, REST_RESOURCE);
        }

        return makeGroupFitness(jsonObject, startDate, endDate);
    }

    @Override
    public GroupFitnessInterface getMultiDayFitness(Date startDate, Date endDate) {
        return null;
    }

    class MyThread extends Thread {

        //RK: Date is hardcoded as of now
        final String resource = REST_RESOURCE+"2017-06-01";

        MyThread(){
        }

        @Override
        public void run() {
            jsonObject = repository.requestJson(context, false, FILENAME, resource);
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
        double calories = jsonObject.getDouble("calories");
        double distance = jsonObject.getDouble("distance");
        double activeMinutes = TimeUnit.MILLISECONDS.toMinutes(date.getTime());
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
        //TODO make string name constant check the funcionality of saving it to local and the first method, actually working?
        editPref.putString("cacheExpiryDate", cacheExpiryDate.toString());
        editPref.apply();
    }
}
