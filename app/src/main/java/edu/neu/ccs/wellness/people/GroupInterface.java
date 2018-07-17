package edu.neu.ccs.wellness.people;

import java.util.List;

/**
 * Created by hermansaksono on 11/3/17.
 */

public interface GroupInterface {

    int getId();

    String getName();

    List<Person> getMembers();

    Person getPersonByRole(String roleString) throws PersonDoesNotExistException;
}
