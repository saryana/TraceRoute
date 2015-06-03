package com.gps.capstone.traceroute;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by saryana on 6/2/15.
 */
public class AdaptingAdapter extends ArrayAdapter<String> {

    private int mVisibility;

    public AdaptingAdapter(Context context, int resource, List<String> objects, boolean isMulti) {
        super(context, resource, objects);
        setMultiSelect(isMulti);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.saved_path, parent, false);
        }
        ((TextView)convertView.findViewById(R.id.path_name)).setText(getItem(position));
        convertView.findViewById(R.id.delete_check_mark).setVisibility(mVisibility);
        return convertView;
    }

    public void setMultiSelect(boolean isMulti) {
        mVisibility = isMulti ? View.VISIBLE : View.GONE;
        notifyDataSetChanged();
    }

}
