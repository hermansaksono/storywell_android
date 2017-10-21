package edu.neu.ccs.wellness;

import android.os.AsyncTask;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static edu.neu.ccs.wellness.storytelling.StoryListFragment.storyIdClicked;


public class StreamReflectionsFirebase extends AsyncTask<Void, Void, Void> {


    private DatabaseReference mDBReference = FirebaseDatabase.getInstance().getReference();
    //This HashMap contains a key which is the Reflection Id i.e. if the reflection page is 6 or 7....
    //The ArrayList corresponding to it contains the list of urls of all the reflections
    //uploaded by the particular user
    public static HashMap<String, Collection<ArrayList<String>>> reflectionsUrl = new HashMap<String, java.util.Collection<ArrayList<String>>>();

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
                                //Now Reflections Url contain something like this:
                                // 6, [https://firebasestorage.googleapis.com/v0/...,
                                //      https://firebasestorage.googleapis.com/v0/...]
                                //Here 6 is the Reflection id and the key for the HashMap
                                //The value if an Array List of Urls of all reflections uploaded
                                //We can simply get the Last url from ArrayList if
                                // we need it just for streaming the last one
                                //We can use others for potential use cases which
                                // might include having a screen for all recordings
                                //And giving user the ability to delete previous recordings
                                // TODO: Get the last one only if required-@Herman
                                reflectionsUrl.put(ds.getKey(), ((Map<String, ArrayList<String>>) ds.getValue()).values());
                            }
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        reflectionsUrl.clear();
                    }
                });


        //Make replay and next buttons visible
        //Change booleans for proper navigation between Fragments
        return null;
    }

}
