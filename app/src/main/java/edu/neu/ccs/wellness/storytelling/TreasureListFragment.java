package edu.neu.ccs.wellness.storytelling;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.neu.ccs.wellness.reflection.TreasureItem;
import edu.neu.ccs.wellness.story.Story;
import edu.neu.ccs.wellness.storytelling.utils.TreasureItemAdapter;
import edu.neu.ccs.wellness.storytelling.viewmodel.TreasureListViewModel;


public class TreasureListFragment extends Fragment {

    private List<TreasureItem> treasureItemList;
    private ListView treasureListView;
    private Parcelable treasureListState;

    public static TreasureListFragment newInstance(){
        return new TreasureListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_treasure_list, container, false);
        this.treasureListView = rootView.findViewById(R.id.treasureListGridview);

        //Load the detailed story on click on story book
        treasureListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

        LiveData<List<TreasureItem>> liveData = viewModel.getTreasureListLiveData();
        liveData.observe(this, new Observer<List<TreasureItem>>() {
            @Override
            public void onChanged(@Nullable List<TreasureItem> dataSnapshot) {
                if (dataSnapshot != null) {
                    treasureListView.setAdapter(new TreasureItemAdapter(getContext(), dataSnapshot));

                    if (treasureListState != null ) {
                        treasureListView.onRestoreInstanceState(treasureListState);
                    }
                    treasureItemList = dataSnapshot;
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        this.treasureListState = treasureListView.onSaveInstanceState();
        super.onPause();
    }

    private void onReflectionPileClick(int position) {
        if (this.treasureItemList != null) {
            startReflectionViewActivity(treasureItemList.get(position));
        }
    }

    private void startReflectionViewActivity(TreasureItem treasureItem) {
        Intent intent = new Intent(getContext(), ReflectionViewActivity.class);
        intent.putExtra(Story.KEY_STORY_ID, treasureItem.getParentId());
        intent.putExtra(Story.KEY_STORY_TITLE, treasureItem.getTitle());
        intent.putExtra(Story.KEY_RESPONSE_TIMESTAMP, treasureItem.getLastUpdateTimestamp());
        intent.putIntegerArrayListExtra(Story.KEY_REFLECTION_LIST, getListOfContents(treasureItem));
        getContext().startActivity(intent);
    }

    private static ArrayList<Integer> getListOfContents(TreasureItem treasureItem) {
        ArrayList<Integer> listOfContents = new ArrayList<>();
        for(String stringId : treasureItem.getContents().keySet()) {
            listOfContents.add(Integer.valueOf(stringId));
        }

        Collections.sort(listOfContents);
        return listOfContents;
    }

    // PRIVATE METHODS

    // PRIVATE ASYNCTASK CLASSES
}