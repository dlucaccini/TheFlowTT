package com.diegolucaccini.theflowtt.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.diegolucaccini.theflowtt.R;
import com.diegolucaccini.theflowtt.dal.DbOpenHelper;
import com.diegolucaccini.theflowtt.dal.beans.JourneyBean;

import java.util.ArrayList;

public class JourneysListActivity extends AppCompatActivity {

    private ArrayList<JourneyBean> journeysData;
    private JourneyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journeys_list);

        journeysData = DbOpenHelper.getInstance(this).getJourneys();

        Log.d("diego", "" + journeysData.toString());

        adapter = new JourneyAdapter();

        ListView list = (ListView) findViewById(R.id.journey_list);
        list.setAdapter(adapter);

    }

    public void clearJourneys(View view) {

        DbOpenHelper.getInstance(this).clearJourneys();
        journeysData = DbOpenHelper.getInstance(this).getJourneys();
        adapter.notifyDataSetChanged();

    }


    // View lookup cache
    private static class ViewHolder {
        TextView start;
        TextView end;
        TextView row;
    }

    private class JourneyAdapter extends BaseAdapter {


        @Override
        public int getCount() {
            return journeysData.size();
        }

        @Override
        public Object getItem(int i) {
            return journeysData.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {

            final JourneyBean j = journeysData.get(i);

            ViewHolder viewHolder;

            if (convertView == null) {

                LayoutInflater inflater = LayoutInflater.from(JourneysListActivity.this);
                convertView = inflater.inflate(R.layout.row_journey, viewGroup, false);

                viewHolder = new ViewHolder();

                viewHolder.row = convertView.findViewById(R.id.row);
                viewHolder.start = convertView.findViewById(R.id.start);
                viewHolder.end = convertView.findViewById(R.id.end);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(JourneysListActivity.this, JourneyDetailActivity.class);
                        i.putExtra("journey", j);
                        startActivity(i);
                    }
                });
                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }


            viewHolder.row.setText("" + (i + 1));
            viewHolder.start.setText("Started: " + j.getStartDate());
            viewHolder.end.setText("Ended: " + (j.getEndDate() == null ? "-" : j.getEndDate()));

            return convertView;
        }
    }

}
