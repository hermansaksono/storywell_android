package edu.neu.ccs.wellness.fitness.unused;

/**
 * Created by lilianngweta on 6/28/17.
 */

/**
 * Describes the activity of a Person in one week
 */
public class Activities {

//    private int id;
//    private String name;
//    private String role;
//    private String date;
//    private int steps;
//
//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getRole() {
//        return role;
//    }
//
//    public void setRole(String role) {
//        this.role = role;
//    }
//
//    public String getDate() {
//        return date;
//    }
//
//    public void setDate(String date) {
//        this.date = date;
//    }
//
//    public int getSteps() {
//        return steps;
//    }
//
//    public void setSteps(int steps) {
//        this.steps = steps;
//    }



    private int id;
    private String name;
    private String[] date;
    private int[] steps; // The steps count in one week

    public String getName() {
        return name;
    }

    public int[] getSteps() {
        return steps;
    }

    /**
     * Given an array of one week steps, sets it as the instance's one week step
     * @param steps
     */
    public void setSteps(int[] steps) {
        this.steps = steps;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String[] getDate() {
        return date;
    }

    public void setDate(String[] date) {
        this.date = date;
    }



    public void setName(String name) {
        this.name = name;
    }

}
