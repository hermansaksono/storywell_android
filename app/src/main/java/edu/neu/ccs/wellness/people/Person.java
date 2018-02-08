package edu.neu.ccs.wellness.people;

import com.google.gson.annotations.SerializedName;

/**
 * Created by hermansaksono on 11/3/17.
 */

public class Person {

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

    /* PUBLIC FUNCTIONS */
    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getRole() { return this.role; }
}
