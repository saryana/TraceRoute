package com.gps.capstone.traceroute.sensors;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gps.capstone.traceroute.R;
import com.gps.capstone.traceroute.Utils.BusProvider;
import com.gps.capstone.traceroute.sensors.events.PathCompletion;
import com.squareup.otto.Subscribe;

public class PathInfo extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";


    // TODO: Rename and change types of parameters
    private int mSteps;
    private float mDistance;
    private float mInitialAltitude;
    private float mFinalAltitude;

    TextView mStepView;
    TextView mDistanceView;
    TextView mInitAltView;
    TextView mFinalAltView;
    TextView mAltChangeView;

//    private OnFragmentInteractionListener mListener;

    public static PathInfo newInstance(int steps, float distance, float initialAltitude, float finalAltitude) {
        PathInfo fragment = new PathInfo();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, steps);
        args.putFloat(ARG_PARAM2, distance);
        args.putFloat(ARG_PARAM3, initialAltitude);
        args.putFloat(ARG_PARAM4, finalAltitude);
        fragment.setArguments(args);
        return fragment;
    }

    public PathInfo() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSteps = getArguments().getInt(ARG_PARAM1);
            mDistance = getArguments().getFloat(ARG_PARAM2);
            mInitialAltitude = getArguments().getFloat(ARG_PARAM3);
            mFinalAltitude = getArguments().getFloat(ARG_PARAM4);
        }
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Subscribe
    public void onPathEnd(PathCompletion path) {
        mSteps = path.steps;
        mDistance = path.distance;
        mInitialAltitude = path.initialAltitude;
        mFinalAltitude = path.finalAltitude;
        mStepView.setText(String.valueOf(mSteps));
        mDistanceView.setText(String.valueOf(Math.round(mDistance / 12)));
        mInitAltView.setText(String.valueOf(Math.round(mInitialAltitude)));
        mFinalAltView.setText(String.valueOf(Math.round(mFinalAltitude)));
        mAltChangeView.setText(String.valueOf(Math.round(mInitialAltitude - mFinalAltitude)));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_path_info, container, false);
        mStepView = (TextView) v.findViewById(R.id.total_steps);
        mDistanceView = (TextView) v.findViewById(R.id.total_distance);
        mInitAltView = (TextView) v.findViewById(R.id.init_alt);
        mFinalAltView = (TextView) v.findViewById(R.id.final_alt);
        mAltChangeView = (TextView) v.findViewById(R.id.alt_change);
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
//        try {
//            mListener = (OnFragmentInteractionListener) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }
}
