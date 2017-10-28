package edu.neu.ccs.wellness.storytelling.interfaces;

import java.util.List;
import java.util.Map;

import edu.neu.ccs.wellness.server.AuthUser;

/**
 * Created by hermansaksono on 6/14/17.
 */

public interface GroupActivityInterface {

    GroupActivityInterface create(AuthUser user, String startDate, String endDate);

    String getStartDate();

    String getEndDate();

    List<PersonInterface> getMembers();

    Map<PersonInterface, List<ActivityInterface>> getDailyActivities();

}
