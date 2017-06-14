package edu.neu.ccs.wellness.storytelling;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import edu.neu.ccs.wellness.storytelling.interfaces.StorytellingManagerInterface;
import edu.neu.ccs.wellness.storytelling.models.StoryManager;
import edu.neu.ccs.wellness.storytelling.models.WellnessRestServer;
import edu.neu.ccs.wellness.storytelling.models.WellnessUser;

public class StoryListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        WellnessUser user = new WellnessUser("family01", "tacos001");
        WellnessRestServer server = new WellnessRestServer("http://wellness.ccs.neu.edu/", 0,
                "storytelling_dev/api/", user);


        StoryManager storyManager = StoryManager.create(server);
    }
}
