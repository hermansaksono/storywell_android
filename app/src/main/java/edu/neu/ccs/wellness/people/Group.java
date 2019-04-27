package edu.neu.ccs.wellness.people;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.server.RestServer;
import edu.neu.ccs.wellness.server.WellnessRestServer;

/**
 * Created by hermansaksono on 11/3/17.
 */

public class Group implements GroupInterface {

    /* CONSTANTS */
    private static final String FILENAME = "group_info.json";
    private static final String RES_GROUP = "group/info";

    /* PRIVATE VARIABLES */
    private int id;
    private String name;
    private List<Person> members;

    /* PRIVATE CONSTRUCTOR */
    private Group() {

    }

    private Group(int id, String name, List<Person> members) {
        this.id = id;
        this.name = name;
        this.members = members;
    }

    /* FACTORY METHODS */
    /**
     * Get the currently logged Group from internal storage, otherwise make a remote call to server
     * @param context Application's Context
     * @param server RestServer to make the call to
     * @return Group object from the saved file in internal storage, or from the Server
     */
    public static Group getInstance(Context context, RestServer server) {
        Group group = null;
        try {
            String jsonString = server.doGetRequestFromAResource(context, FILENAME, RES_GROUP, WellnessRestServer.USE_SAVED);
            group = new Gson().fromJson(jsonString, Group.class);
        } catch (IOException e) {
            Log.e("SWELL", "Fetching group error: " + e.getMessage());
        }
        return group;
    }

    /**
     * Get the currently logged Group from internal storage, otherwise make a remote call to server
     * @param context Application's Context
     * @param isUseSaved Whether use the saved instance
     * @param server RestServer to make the call to.
     * @return Group object from the saved file in internal storage, or from the Server
     */
    public static Group getInstance(Context context, boolean isUseSaved, RestServer server) {
        Group group = null;
        try {
            String jsonString = server.doGetRequestFromAResource(context, FILENAME, RES_GROUP, isUseSaved);
            group = new Gson().fromJson(jsonString, Group.class);
        } catch (IOException e) {
            Log.e("SWELL", "Fetching group error: " + e.getMessage());
        }
        return group;
    }

    /**
     * Delete the instance of the Group.
     * @param context
     * @param restServer
     * @return
     */
    public static boolean deleteInstance(Context context, RestServer restServer) {
        if (restServer.isFileExists(context, FILENAME)) {
            context.deleteFile(FILENAME);
            return true;
        } else {
            return false;
        }
    }

    /* PUBLIC METHODS */
    @Override
    public int getId() {
        return 0;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public List<Person> getMembers() {
        return this.members;
    }

    @Override
    public Person getPersonByRole(String roleString) throws PersonDoesNotExistException{
        Person thePerson = null;

        for (Person person : members) {
            if (person.isRole(roleString)) {
                thePerson = person;
                break;
            }
        }

        if (thePerson != null) {
            return thePerson;
        } else {
            throw new PersonDoesNotExistException("No person with role " + roleString);
        }
    }

    /* PRIVATE HELPER METHODS */
    private static Group getInstanceFromJsonString(String jsonString){
        Group group = null;
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            if (jsonObject.getInt("id") >= 0)
                Log.d("WELL", String.valueOf(jsonObject.getString("name")));
            group = new Group(
                    jsonObject.getInt("id"),
                    jsonObject.getString("name"),
                    getMembersFromJson(jsonObject.getJSONArray("members")));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return group;
    }

    private static List<Person> getMembersFromJson (JSONArray jsonArray)
            throws JSONException {
        ArrayList<Person> members = new ArrayList<Person>();
        for(int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonPerson = jsonArray.getJSONObject(i);
            members.add(newPersonInstanceFromJsonObject(jsonPerson));
        }
        return members;
    }

    private static Person newPersonInstanceFromJsonObject(JSONObject jsonObj)
            throws JSONException {
        return Person.newInstance(jsonObj.getInt("id"), jsonObj.getString("name"), jsonObj.getString("role"));
    }
}
