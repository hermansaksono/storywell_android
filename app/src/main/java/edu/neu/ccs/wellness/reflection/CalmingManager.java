package edu.neu.ccs.wellness.reflection;

import android.content.Context;

/**
 * Created by hermansaksono on 3/4/19.
 */

public class CalmingManager extends ReflectionManager {
    private static final String REFLECTION_FILENAME_FORMAT = "/calming_%s_content_%s.3gp";

    public CalmingManager(String groupName, String storyId,
                          int reflectionIteration, long reflectionMinEpoch,
                          Context context) {
        super(groupName, storyId, reflectionIteration, reflectionMinEpoch,
                REFLECTION_FILENAME_FORMAT, context);
    }
}
