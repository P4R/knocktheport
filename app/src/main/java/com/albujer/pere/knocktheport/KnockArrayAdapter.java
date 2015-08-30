package com.albujer.pere.knocktheport;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.albujer.pere.knocktheport.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class KnockArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private  JSONArray jsonArray;
    private int lastPosition = 0;

    public KnockArrayAdapter(Context context, List values) {
        super(context, R.layout.knock_list_item, values);
        this.context = context;
        try {
            this.jsonArray = new JSONArray(values.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.knock_list_item, parent, false);
        TextView tvName = (TextView) rowView.findViewById(R.id.name);
        TextView tvPorts = (TextView) rowView.findViewById(R.id.ports);

        try {
            JSONObject jsonObject = this.jsonArray.getJSONObject(position);
            String strPorts = jsonObject.get("ports").toString();
            strPorts = strPorts.replace(", ",":");
            strPorts = strPorts.replace("]:[",", ");
            strPorts = strPorts.replace("[","");
            strPorts = strPorts.replace("]","");
            tvName.setText(jsonObject.get("name").toString());
            tvPorts.setText(strPorts);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        TranslateAnimation animation = null;
        if (position >= lastPosition) {
            animation = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF,
                    0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f);
            animation.setDuration(400);
            animation.setInterpolator(new DecelerateInterpolator(1.0f));
            lastPosition = position;
        }
        else {
            animation = new TranslateAnimation(
                    Animation.RELATIVE_TO_SELF,
                    0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                    Animation.RELATIVE_TO_SELF, -1.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f);
            animation.setDuration(400);
            animation.setInterpolator(new DecelerateInterpolator(1.0f));
            lastPosition = position;
        }
        rowView.startAnimation(animation);
        return rowView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        lastPosition = -1;
    }

}