package edu.neu.ccs.wellness.utils;

import android.os.AsyncTask;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import static edu.neu.ccs.wellness.storytelling.StoryListFragment.storyIdClicked;


public class StreamReflectionsFirebase extends AsyncTask<Void, Void, Void> {


    private DatabaseReference mDBReference = FirebaseDatabase.getInstance().getReference();
    //This HashMap contains a key which is the Reflection Id i.e. if the reflection page is 6 or 7....
    //The ArrayList corresponding to it contains the list of urls of all the reflections
    //uploaded by the particular user
    public static HashMap<Integer, String> reflectionsUrlHashMap
            = new HashMap<Integer, String>();

    /***************************************************************************
     * STREAM FROM DATABASE
     * If file is deleted and user comes back, stream the content
     ***************************************************************************/
    @Override
    protected Void doInBackground(Void... voids) {

        //If the download URL field of the user is not null
        //That means there is a reflection already
        //Get The Reflection audio for Streaming
        //Give the Path to the file depending on the user

        //PATH: USER_ID --> Story_ID -> Reflection_Id(get the URL for both)
        //              --> Parse the url and hit them to get content on the go
        mDBReference
                .child("USER_ID")
                //Get the story Id from the grid listener on the home screen
                .child(String.valueOf(storyIdClicked))
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //Check if the download URL field is null
                        //If null, do not stream and go normally
                        //If not null, stream content
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                //TODO: Get only the last reflection
                                //Don't get all Reflections
                                //Right now we need all for testing
                                List<Object> listOfUrls
                                        = new ArrayList<>((Collection<?>)
                                        ((HashMap<Object, Object>) ds.getValue()).values());

                                String targetUrl = (String) listOfUrls.get(listOfUrls.size() - 1);
                                //Here 6 is the Reflection id and the key for the HashMap
                                reflectionsUrlHashMap.put(Integer.parseInt(ds.getKey()), targetUrl);
                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        reflectionsUrlHashMap.clear();
                    }
                });


        //Make replay and next buttons visible
        //Change booleans for proper navigation between Fragments
        return null;
    }

}