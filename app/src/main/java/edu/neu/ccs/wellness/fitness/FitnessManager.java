package edu.neu.ccs.wellness.fitness;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import edu.neu.ccs.wellness.fitness.interfaces.FitnessManagerInterface;
import edu.neu.ccs.wellness.fitness.interfaces.GroupFitnessInterface;
import edu.neu.ccs.wellness.fitness.interfaces.OneDayFitnessInterface;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.WellnessRepository;
import edu.neu.ccs.wellness.utils.WellnessIO;

/**
 * Created by hermansaksono on 6/21/17.
 */

public class FitnessManager implements FitnessManagerInterface {

    // PRIVATE VARIABLES
    private static final String REST_RESOURCE = "group/activities/7d/";
    private static final String JSON_DATEE_FORMAT = "yyyy-mm-dd";
    private static final String FILENAME = "FitnessManager.json";
    private static final int FIFTEEN_MINUTES = 900000; // TODO HS: Why not 15 * 60 * 60
    private Context context;
    private JSONObject jsonObject;
    private WellnessRepository repository;

    /* CONSTRUCTOR */
    private FitnessManager(RestServer server, Context context) {
        this.context = context;
        this.repository = new WellnessRepository(server, context);
    }

    public static FitnessManagerInterface create(RestServer server, Context context){
        return new FitnessManager(server, context);
    }

    /* INTERFACE METHODS */
    @Override
    public GroupFitnessInterface getMultiDayFitness(Date startDate, Date endDate, Date cacheExpiryDate) {
        Date date = new Date();
        String resource = REST_RESOURCE + "2017-06-01"; //TODO RK: Date is hardcoded as of now

        if(date.after(cacheExpiryDate)){
            jsonObject = repository.requestJson(context, false, FILENAME, resource);
            saveNewCacheExpiryDate(cacheExpiryDate);
        } else {
            jsonObject = repository.requestJson(this.context, true, FILENAME, resource);
        }

        return makeGroupFitness(jsonObject, startDate, endDate);
    }

    /* PRIVATE HELPER METHODS */
    private GroupFitnessInterface makeGroupFitness(JSONObject jsonObject, Date startDate, Date endDate){
        Map<Person, MultiDayFitness> groupFitness = null;
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("activities");
            groupFitness = getGroupMultiDayFitness(jsonArray, startDate, endDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return GroupFitness.create(context, groupFitness);
    }

    private Map<Person, MultiDayFitness> getGroupMultiDayFitness(JSONArray jsonArray, Date startDate, Date endDate)
            throws JSONException {
        Map<Person, MultiDayFitness> groupMultiDayFitnessMap = new HashMap<>();
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject onePersonJsonObject = (JSONObject) jsonArray.get(i);
            Person person = Person.newInstance(onePersonJsonObject);
            MultiDayFitness multiDayFitness = makeMultiDayFitness(onePersonJsonObject, startDate, endDate);

            groupMultiDayFitnessMap.put(person, multiDayFitness);
        }
        return groupMultiDayFitnessMap;
    }

    private MultiDayFitness makeMultiDayFitness(JSONObject eachGroupMemberJson, Date startDate, Date endDate){
        List<OneDayFitnessInterface> oneDayFitnesses = new ArrayList<>();
        int numberOfDays = 7;
        int elapsedDays = 0;
        try {
            JSONArray jsonArray = eachGroupMemberJson.getJSONArray("activities");
            elapsedDays = jsonArray.length();
            for (int i = 0; i<elapsedDays; i++){
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                OneDayFitness oneDayFitness = makeOneDayFitness(jsonObject);
                oneDayFitnesses.add(oneDayFitness);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return MultiDayFitness.create(context, startDate, endDate, numberOfDays, elapsedDays, oneDayFitnesses);
    }

    private OneDayFitness makeOneDayFitness(JSONObject jsonObject) throws JSONException, ParseException {
        DateFormat d = new SimpleDateFormat(JSON_DATEE_FORMAT);
        Date date = d.parse(jsonObject.getString("date"));
        int steps = jsonObject.getInt("steps");
        double calories = jsonObject.getDouble("calories");
        double distance = jsonObject.getDouble("distance");
        double activeMinutes = TimeUnit.MILLISECONDS.toMinutes(date.getTime());
        return OneDayFitness.create(context, date, steps, calories, distance, activeMinutes);
    }


    private void saveNewCacheExpiryDate(Date cacheExpiryDate){
        cacheExpiryDate.setTime(cacheExpiryDate.getTime() + FIFTEEN_MINUTES);
        SharedPreferences sharedPrefs = WellnessIO.getSharedPref(this.context);
        SharedPreferences.Editor editPref = sharedPrefs.edit();
        //TODO make string name constant check the funcionality of saving it to local and the first method, actually working?
        editPref.putString("cacheExpiryDate", cacheExpiryDate.toString());
        editPref.apply();
    }
}
