package edu.neu.ccs.wellness.storytelling.firstrun;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import edu.neu.ccs.wellness.storytelling.R;

import static edu.neu.ccs.wellness.storytelling.firstrun.CheckFirstRun.mViewPagerFirstRun;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AskPermissionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AskPermissionsFragment extends Fragment {

    //Request Audio Permissions as AUDIO RECORDING falls under DANGEROUS PERMISSIONS
    private final int REQUEST_AUDIO_PERMISSIONS = 100;
    private String[] permission = {android.Manifest.permission.RECORD_AUDIO};


    public AskPermissionsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AskPermissionsFragment.
     */
    public static AskPermissionsFragment newInstance() {
        AskPermissionsFragment fragment = new AskPermissionsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_ask_permissions_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getView().findViewById(R.id.requestAudioButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(getContext(),
                        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(permission, REQUEST_AUDIO_PERMISSIONS);
                } else {
                    Toast.makeText(getContext(), "Permission available", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Get the requestCode and check our case
        switch (requestCode) {
            case REQUEST_AUDIO_PERMISSIONS:
                //If Permission is Granted, change the boolean value
                if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    mViewPagerFirstRun.setCurrentItem(2);
                } else {
                    showSnackBar("Audio Permission needed. Please consider again");
                }
                break;
        }
    }


    private void showSnackBar(String message){
        //Permission not granted
        Snackbar permissionsSnackBar = Snackbar.make(getView().findViewById(R.id.parentFrame),
                message, Snackbar.LENGTH_LONG);
        View view = permissionsSnackBar.getView();
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        params.gravity = Gravity.CENTER;
        view.setLayoutParams(params);

        permissionsSnackBar.setAction("Try Again", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(getActivity(),
                        permission, REQUEST_AUDIO_PERMISSIONS);
            }
        });
        permissionsSnackBar.show();
    }

}
