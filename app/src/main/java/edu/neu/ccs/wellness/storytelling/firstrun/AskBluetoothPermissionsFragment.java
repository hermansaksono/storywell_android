package edu.neu.ccs.wellness.storytelling.firstrun;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import edu.neu.ccs.wellness.utils.WellnessBluetooth;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AskBluetoothPermissionsFragment#newInstance} factory method to
 * newInstance an instance of this fragment.
 */
public class AskBluetoothPermissionsFragment extends Fragment {

    private OnPermissionChangeListener permissionListener;


    public AskBluetoothPermissionsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to newInstance a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AskAudioRecordingPermissionsFragment.
     */
    public static AskBluetoothPermissionsFragment newInstance() {
        AskBluetoothPermissionsFragment fragment = new AskBluetoothPermissionsFragment();
        return fragment;
    }

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_firstrun_bluetoothpermission, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getView().findViewById(R.id.requestCoarseLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tryRequestPermission();
            }
        });
    }

    private void tryRequestPermission() {
        if (WellnessBluetooth.isCoarseLocationAllowed(this.getContext())) {
            permissionListener.onPermissionGranted();
        } else {
            ActivityCompat.requestPermissions(getActivity(), WellnessBluetooth.COARSE_PERMISSIONS,
                    WellnessBluetooth.PERMISSION_REQUEST_COARSE_LOCATION);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            this.permissionListener = (OnPermissionChangeListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(((Activity) context).getLocalClassName()
                    + " must implement OnPermissionChangeListener");
        }
    }

    public static boolean isPermissionGranted(int[] grantResults) {
        return grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
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
                WellnessBluetooth.tryRequestCoarsePermission(getActivity());
            }
        });
        permissionsSnackBar.show();
    }

}
