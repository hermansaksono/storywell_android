package edu.neu.ccs.wellness.storytelling;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import edu.neu.ccs.wellness.storytelling.interfaces.StorytellingManagerInterface;
import edu.neu.ccs.wellness.storytelling.models.StoryManager;

public class StoryListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        StorytellingManagerInterface storyManager = new StoryManager();
    }
}
