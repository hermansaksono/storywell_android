package edu.neu.ccs.wellness.storytelling;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

import edu.neu.ccs.wellness.reflection.ResponsePile;
import edu.neu.ccs.wellness.story.Story;
import edu.neu.ccs.wellness.storytelling.utils.TreasureItemAdapter;
import edu.neu.ccs.wellness.storytelling.viewmodel.TreasureListViewModel;


public class TreasureListFragment extends Fragment {

    private List<ResponsePile> responsePiles;
    private GridView gridview;

    public static TreasureListFragment newInstance(){
        return new TreasureListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_treasure_list, container, false);
        this.gridview = rootView.findViewById(R.id.treasureListGridview);

        //Load the detailed story on click on story book
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                onReflectionPileClick(position);
            }
        });

        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TreasureListViewModel viewModel = ViewModelProviders.of(this)
                .get(TreasureListViewModel.class);

        LiveData<List<ResponsePile>> liveData = viewModel.getTreasureListLiveData();
        liveData.observe(this, new Observer<List<ResponsePile>>() {
            @Override
            public void onChanged(@Nullable List<ResponsePile> dataSnapshot) {
                if (dataSnapshot != null) {
                    gridview.setAdapter(new TreasureItemAdapter(getContext(), dataSnapshot));
                    responsePiles = dataSnapshot;
                }
            }
        });
    }

    private void onReflectionPileClick(int position) {
        if (this.responsePiles != null) {
            startReflectionViewActivity(responsePiles.get(position));
        }
    }

    private void startReflectionViewActivity(ResponsePile responsePile) {
        Intent intent = new Intent(getContext(), ReflectionViewActivity.class);
        intent.putExtra(Story.KEY_STORY_ID, Integer.toString(responsePile.getStoryId()));
        intent.putExtra(Story.KEY_STORY_TITLE, responsePile.getTitle());
        intent.putExtra(Story.KEY_RESPONSE_TIMESTAMP, responsePile.getTimestamp());
        intent.putStringArrayListExtra(Story.KEY_REFLECTION_LIST, getListOfPages(responsePile));
        getContext().startActivity(intent);
    }

    private static ArrayList<String> getListOfPages(ResponsePile responsePile) {
        return new ArrayList<>(responsePile.getPiles().keySet());
    }

    // PRIVATE METHODS

    // PRIVATE ASYNCTASK CLASSES
}