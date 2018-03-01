package edu.neu.ccs.wellness.fitness.challenges;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.fitness.interfaces.ChallengeStatus;
import edu.neu.ccs.wellness.fitness.interfaces.AvailableChallengesInterface;

/**
 * Created by hermansaksono on 10/16/17.
 */

public class AvailableChallenges implements AvailableChallengesInterface {
    public static final String STRING_FORMAT = "%s - %s";


    private ChallengeStatus status = ChallengeStatus.UNSTARTED;
    private String text;
    private String subtext;
    private List<Challenge> challenges = null;

    private AvailableChallenges() { }

    public static AvailableChallenges create(String jsonString) throws JSONException {
        AvailableChallenges groupChallenge = new AvailableChallenges();
        groupChallenge.processChallengesFromJsonString(jsonString);
        return groupChallenge;
    }

    public static AvailableChallenges create(JSONObject jsonObject) {
        AvailableChallenges groupChallenge = new AvailableChallenges();
        groupChallenge.processChallengesFromJsonObject(jsonObject);
        return groupChallenge;
    }

    @Override
    public String getText() { return this.text; }

    @Override
    public String getSubtext() { return this.subtext; }

    @Override
    public String toString() {
        return String.format(STRING_FORMAT, this.text, this.subtext);
    }

    @Override
    public List<Challenge> getChallenges() {
        return this.challenges;
    }

    /*
    public List<PersonChallenge> getCurrentProgress() {
        return this.personChallenges;
    }

    public RestServer.ResponseType loadChallenges(Context context, WellnessRestServer server) {
        RestServer.ResponseType response = null;
        try {
            String jsonString = server.doGetRequestFromAResource(context, FILENAME_CHALLENGES, RES_CHALLENGES, true);
            this.processChallengesFromJsonString(jsonString);
            response = RestServer.ResponseType.SUCCESS_202;
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            response = RestServer.ResponseType.NOT_FOUND_404;
        }
        return response;
    }
    */

    /*
    public RestServer.ResponseType postAvailableChallenge(Challenge challenge,
                                                      WellnessRestServer server) {
        RestServer.ResponseType response = null;
        try {
            String jsonString = server.doPostRequestFromAResource(challenge.getJsonText(), RES_CHALLENGES);
            this.processChallengesFromJsonString(jsonString);
            response = RestServer.ResponseType.SUCCESS_202;
        }
        catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            //response = RestServer.ResponseType.NOT_FOUND_404;
        }
        return response;
    }
    */

    private void processChallengesFromJsonString(String jsonString) throws JSONException {
        JSONObject jsonObject = new JSONObject(jsonString);
        processChallengesFromJsonObject(jsonObject);
    }



    private void processChallengesFromJsonObject(JSONObject jsonObject) {
        try {
            this.text = jsonObject.getString("text");
            this.subtext =  jsonObject.getString("subtext");
            this.challenges = new ArrayList<>();
            JSONArray challengeArray = jsonObject.getJSONArray("challenges");
            Challenge challenge = null;
            JSONObject jsonChallenge = null;
            for(int i = 0; i < challengeArray.length(); i++) {
                challenge = new Challenge(challengeArray.getJSONObject(i));
                challenges.add(challenge);
            }
//            if (jsonObject.getBoolean("is_currently_running") == false) {
//                this.status = ChallengeStatus.AVAILABLE;
//                this.challenges = ChallengeManager.getListOfAvailableChallenges(jsonObject);
//            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
