package edu.neu.ccs.wellness.reflection;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hermansaksono on 1/17/19.
 */

public class ResponsePileListFactory {
    public static final String FIREBASE_REFLECTION_PILE = "group_reflections_pile";

    /**
     * Process a DataSnapshot with a valid format to return a list of ResponsePile
     * @param pilesOfIterations
     * @return A list of ResponsePile given a DataSnapshot that contains responses of a group.
     */
    public static List<ResponsePile> newInstance(DataSnapshot pilesOfIterations) {
        List<ResponsePile> reflectionPile = new ArrayList<>();
        if (pilesOfIterations.exists()) {
            for (DataSnapshot oneIteration : pilesOfIterations.getChildren()) {
                reflectionPile.addAll(getPilesFromOneIteration(oneIteration));
            }
        }
        return reflectionPile;
    }

    private static List<ResponsePile> getPilesFromOneIteration(DataSnapshot oneIteration) {
        List<ResponsePile> reflectionPileFromOneIncarnation = new ArrayList<>();
        if (oneIteration.exists()) {
            for (DataSnapshot oneStory : oneIteration.getChildren()) {
                String storyIdString = oneStory.getKey();
                int storyId = Integer.valueOf(storyIdString);
                reflectionPileFromOneIncarnation.addAll(getPilesFromOneStory(oneStory, storyId));
            }
        }
        return reflectionPileFromOneIncarnation;
    }

    private static List<ResponsePile> getPilesFromOneStory(DataSnapshot dataSnapshot, int storyId) {
        List<ResponsePile> reflectionPileFromOneStory= new ArrayList<>();
        if (dataSnapshot.exists()) {
            for (DataSnapshot reflectionGroup : dataSnapshot.getChildren()) {
                ResponsePile pile = getOnePileFromOneStory(reflectionGroup, storyId);
                if (pile != null) {
                    reflectionPileFromOneStory.add(pile);
                }
            }
        }
        return reflectionPileFromOneStory;
    }

    private static ResponsePile getOnePileFromOneStory(DataSnapshot dataSnapshot, int storyId) {
        if (dataSnapshot.exists()) {
            return new ResponsePile(
                    storyId,
                    getResponsePileTitle(dataSnapshot),
                    getReflectionsMap(dataSnapshot),
                    getTimestamp(dataSnapshot),
                    getType(dataSnapshot));

        } else {
            return null;
        }
    }

    private static String getResponsePileTitle(DataSnapshot dataSnapshot) {
        DataSnapshot pileTitleDS = dataSnapshot.child(ResponsePile.KEY_RESPONSE_GROUP_NAME);
        if (pileTitleDS.exists()) {
            return pileTitleDS.getValue(String.class);
        } else {
            return "Recordings";
        }
    }

    private static Map<String, String> getReflectionsMap(DataSnapshot dataSnapshot) {
        Map<String, String> reflectionList = new HashMap<>();
        DataSnapshot responseDataSnapshot = dataSnapshot.child(ResponsePile.KEY_RESPONSE_PILE);
        if (responseDataSnapshot.exists()){
            for (DataSnapshot oneReflection : responseDataSnapshot.getChildren()) {
                reflectionList.put(oneReflection.getKey(), (String) oneReflection.getValue());
            }
        }
        return reflectionList;
    }

    private static long getTimestamp(DataSnapshot dataSnapshot) {
        DataSnapshot dsTimestamp = dataSnapshot.child(ResponsePile.KEY_RESPONSE_TIMESTAMP);
        if (dsTimestamp.exists()) {
            return dsTimestamp.getValue(Long.class);
        } else {
            return 0;
        }
    }

    private static int getType(DataSnapshot dataSnapshot) {
        if (dataSnapshot.child(ResponsePile.KEY_RESPONSE_TYPE).exists()) {
            return dataSnapshot.child(ResponsePile.KEY_RESPONSE_TYPE).getValue(Integer.class);
        } else {
            return TreasureItemType.STORY_REFLECTION;
        }
    }
}
