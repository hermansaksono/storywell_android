package edu.neu.ccs.wellness.people;

import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hermansaksono on 11/3/17.
 */

public class Person {
    /* PUBLIC STATIC VARIABLES */
    public final static String ROLE_PARENT = "P";
    public final static String ROLE_CHILD = "C";

    /* PRIVATE VARIABLES */
    private int id;
    private String name;
    private String role;

    /* CONSTRUCTORS */
    public Person(int id, String name, String role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    /* FACTORY METHODS */
    public static Person newInstance(int id, String name, String role) {
        return new Person(id, name, role);
    }

    public static Person newInstance(JSONObject jsonObject){
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

    /* PUBLIC FUNCTIONS */

    @Override
    public boolean equals(Object obj) {

        if(this == obj)
            return true;

        if(obj == null || obj.getClass()!= this.getClass())
            return false;

        Person thisPerson = (Person) obj;
        return (thisPerson.name.equals(this.name) && thisPerson.id == this.id);
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getRole() { return this.role; }

    public boolean isRole(String roleString) { return this.getRole().equals(roleString);}
}
