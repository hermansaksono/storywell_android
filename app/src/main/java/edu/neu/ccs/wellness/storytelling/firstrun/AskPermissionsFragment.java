package edu.neu.ccs.wellness.storytelling.firstrun;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
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

import edu.neu.ccs.wellness.storytelling.R;
import edu.neu.ccs.wellness.storytelling.utils.OnFragmentLockListener;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AskPermissionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AskPermissionsFragment extends Fragment {

    public interface OnAudioPermissionListener {
        void onAudioPermissionGranted();
    }

    private final int REQUEST_AUDIO_PERMISSIONS = 100;

    private String[] permission = {android.Manifest.permission.RECORD_AUDIO};
    private OnAudioPermissionListener audioPermissionListener;
    private OnFragmentLockListener fragmentLockListener;


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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_firstrun_audiopermission, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getView().findViewById(R.id.requestAudioButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryRequestPermission();
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
                if (isRecordingGranted(grantResults)) {
                    this.fragmentLockListener.unlockFragmentPager();
                    this.audioPermissionListener.onAudioPermissionGranted();
                } else {
                    showSnackBar(getString(R.string.firstrun_snackbar_mustsetaudio));
                }
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.audioPermissionListener = (OnAudioPermissionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(((Activity) context).getLocalClassName()
                    + " must implement OnAudioPermissionListener");
        }
        try {
            this.fragmentLockListener = (OnFragmentLockListener) context;
            if (isRecordingAllowed() == false)
                this.fragmentLockListener.lockFragmentPager();
        } catch (ClassCastException e) {
            throw new ClassCastException(((Activity) context).getLocalClassName()
                    + " must implement OnFragmentLockListener");
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    private void tryRequestPermission() {
        if (isRecordingAllowed() == false) {
            requestPermissions(permission, REQUEST_AUDIO_PERMISSIONS);
        } else {
            audioPermissionListener.onAudioPermissionGranted();
        }
    }

    private boolean isRecordingAllowed() {
        int permissionRecordAudio = ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.RECORD_AUDIO);
        return permissionRecordAudio == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isRecordingGranted(@NonNull int[] grantResults) {
        return grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED);
    }

    private void showSnackBar(String message){
        Snackbar permissionsSnackBar = Snackbar.make(getView(), message, Snackbar.LENGTH_LONG);
        View view = permissionsSnackBar.getView();
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
        params.gravity = Gravity.BOTTOM;
        view.setLayoutParams(params);

        permissionsSnackBar.setAction(getString(R.string.firstrun_snackbar_tryagain),
                new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryRequestPermission();
            }
        });
        permissionsSnackBar.show();
    }

}
