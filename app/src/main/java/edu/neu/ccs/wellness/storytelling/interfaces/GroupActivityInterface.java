package edu.neu.ccs.wellness.storytelling.interfaces;

import java.util.List;
import java.util.Map;

/**
 * Created by hermansaksono on 6/14/17.
 */

public interface GroupActivityInterface {

    public GroupActivityInterface create(UserAuthInterface user, String startDate, String endDate);

    public String getStartDate();

    public String getEndDate();

    public List<PersonInterface> getMembers();

    public Map<PersonInterface, List<ActivityInterface>> getDailyActivities();

}
