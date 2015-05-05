package com.gps.capstone.traceroute.sensors;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gps.capstone.traceroute.GLFiles.MySurfaceView;
import com.gps.capstone.traceroute.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OpenGLFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OpenGLFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OpenGLFragment extends Fragment {

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment OpenGLFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OpenGLFragment newInstance(String param1, String param2) {
        return new OpenGLFragment();
    }

    public OpenGLFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return new MySurfaceView(getActivity());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
