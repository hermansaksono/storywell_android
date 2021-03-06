package edu.neu.ccs.wellness.fitness;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import edu.neu.ccs.wellness.fitness.interfaces.FitnessException;
import edu.neu.ccs.wellness.fitness.interfaces.FitnessRepositoryInterface;
import edu.neu.ccs.wellness.fitness.interfaces.GroupFitnessInterface;
import edu.neu.ccs.wellness.fitness.interfaces.MultiDayFitnessInterface;
import edu.neu.ccs.wellness.fitness.interfaces.OneDayFitnessInterface;
import edu.neu.ccs.wellness.people.Person;
import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.WellnessRepository;
import edu.neu.ccs.wellness.utils.WellnessIO;

/**
 * Created by hermansaksono on 6/21/17.
 */

public class WellnessFitnessRepo implements FitnessRepositoryInterface {

    // PRIVATE VARIABLES
    public static final String JSON_DATE_FORMAT = "yyyy-MM-dd";
    private static final String REST_RESOURCE = "group/activities/7d/";
    private static final String FILENAME = "WellnessFitnessRepo.json";
    private static final String SHAREDPREF_CACHE_EXPIRY = "CACHE_EXPIRY_DATETIME";
    private static final String DEFAULT_EXPIRY = "2001-01-01";
    private static final int FIFTEEN_MINUTES = 15;
    private Context context;
    private WellnessRepository repository;

    /* CONSTRUCTOR */
    private WellnessFitnessRepo(RestServer server, Context context) {
        this.context = context.getApplicationContext();
        this.repository = new WellnessRepository(server, context);
    }

    /* FACTORY METHOD */
    public static FitnessRepositoryInterface newInstance(RestServer server, Context context) {
        return new WellnessFitnessRepo(server, context);
    }

    /* INTERFACE METHODS */
    @Override
    public GroupFitnessInterface getMultiDayFitness(Date startDate, Date endDate)
            throws FitnessException {
        return getMultiDayFitness(startDate, endDate, getCacheExpiryDate());
    }

    @Override
    public GroupFitnessInterface getMultiDayFitness(Date startDate, Date endDate, Date cacheExpiry)
            throws FitnessException {
        boolean useCachedData = isCacheStillValid(cacheExpiry);
        String resource = REST_RESOURCE + getDateString(startDate);
        if(useCachedData){
            setCacheExpiryAfterThisMinutes(FIFTEEN_MINUTES);
        }
        //Log.d("SWELL", "Fetching Fitness data (" + startDate.toString() + ", useCache=" + useCachedData + ") from: " + resource);
        try {
            JSONObject jsonObject = repository.requestJson(context, useCachedData, FILENAME, resource);
            return makeGroupFitness(jsonObject, startDate, endDate);
        } catch (JSONException e) {
            throw new FitnessException(FitnessException.FitnessErrorType.JSON_EXCEPTION, e.getMessage());
        } catch (IOException e) {
            throw new FitnessException(FitnessException.FitnessErrorType.IO_EXCEPTION, e.getMessage());
        }
    }

    /* PRIVATE HELPER METHODS */
    private GroupFitnessInterface makeGroupFitness(JSONObject jsonObject, Date startDate, Date endDate){
        Map<Person, MultiDayFitnessInterface> groupFitness = null;
        try {
            JSONArray jsonArray = jsonObject.getJSONArray("activities");
            groupFitness = getGroupMultiDayFitness(jsonArray, startDate, endDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return GroupFitness.newInstance(groupFitness);
    }

    private Map<Person, MultiDayFitnessInterface> getGroupMultiDayFitness(JSONArray jsonArray, Date startDate, Date endDate)
            throws JSONException {
        Map<Person, MultiDayFitnessInterface> groupMultiDayFitnessMap = new HashMap<>();
        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject onePersonJsonObject = (JSONObject) jsonArray.get(i);
            Person person = Person.newInstance(onePersonJsonObject);
            MultiDayFitness multiDayFitness = makeMultiDayFitness(onePersonJsonObject, startDate, endDate);

            groupMultiDayFitnessMap.put(person, multiDayFitness);
        }
        return groupMultiDayFitnessMap;
    }

    private MultiDayFitness makeMultiDayFitness(JSONObject eachGroupMemberJson, Date startDate, Date endDate){
        List<OneDayFitnessInterface> multiDayFitness = new ArrayList<>();
        int numberOfDays = 7;
        int elapsedDays = 0;
        try {
            JSONArray jsonArray = eachGroupMemberJson.getJSONArray("activities");
            elapsedDays = jsonArray.length();
            for (int i = 0; i<elapsedDays; i++){
                JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                OneDayFitness oneDayFitness = makeOneDayFitness(jsonObject);
                multiDayFitness.add(oneDayFitness);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return MultiDayFitness.newInstance(startDate, endDate, numberOfDays, elapsedDays, multiDayFitness);
    }

    private OneDayFitness makeOneDayFitness(JSONObject jsonObject) throws JSONException, ParseException {
        DateFormat d = new SimpleDateFormat(JSON_DATE_FORMAT);
        Date date = d.parse(jsonObject.getString("date"));
        int steps = jsonObject.getInt("steps");
        double calories = jsonObject.getDouble("calories");
        double distance = jsonObject.getDouble("distance");
        double activeMinutes = TimeUnit.MILLISECONDS.toMinutes(date.getTime());
        return OneDayFitness.newInstance(date, steps, calories, distance, activeMinutes);
    }

    private boolean isCacheStillValid(Date cacheExpiry) {
        Date date = new Date();
        return date.before(cacheExpiry);
    }

    private Date getCacheExpiryDate() {
        SharedPreferences editPref = WellnessIO.getSharedPref(this.context);
        String dateString = editPref.getString(SHAREDPREF_CACHE_EXPIRY, DEFAULT_EXPIRY);
        return getDate(dateString);
    }

    private void setCacheExpiryAfterThisMinutes(int minutes) {
        Date currentDate = new Date();
        Date expiryDate = getExpiry(currentDate, minutes);
        SharedPreferences.Editor editPref = WellnessIO.getSharedPref(this.context).edit();
        editPref.putString(SHAREDPREF_CACHE_EXPIRY, getDateString(expiryDate));
        editPref.apply();
    }

    private static Date getDate(String dateString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(JSON_DATE_FORMAT);
            return sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
    }

    public static String getDateString(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(JSON_DATE_FORMAT);
        return sdf.format(date);
    }

    private static Date getExpiry(Date date, int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.MINUTE, minutes);
        return cal.getTime();
    }
}
