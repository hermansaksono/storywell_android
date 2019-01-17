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
                ResponsePile pile = new ResponsePile(storyId, getPilesFromOneGroup(reflectionGroup));
                reflectionPileFromOneStory.add(pile);
            }
        }
        return reflectionPileFromOneStory;
    }

    private static Map<String, String> getPilesFromOneGroup(DataSnapshot dataSnapshot) {
        Map<String, String> reflectionList = new HashMap<>();
        if (dataSnapshot.exists()) {
            DataSnapshot responseDataSnapshot = dataSnapshot.child(ResponsePile.KEY_RESPONSE_PILE);
            for (DataSnapshot oneReflection : responseDataSnapshot.getChildren()) {
                reflectionList.put(oneReflection.getKey(), (String) oneReflection.getValue());
            }
        }
        return reflectionList;
    }
}
