package com.example.bravo.findmybus;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

class busRowAdapter extends ArrayAdapter<busList> {

    public static final String TAG = "FindMyBus";
    private ArrayList<busList> bus = new ArrayList<>();
    private TextView bus_no;
    private TextView[] stop = new TextView[10];

    public busRowAdapter(Context context, ArrayList<busList> arrayList) {
        super(context,R.layout.bus_single_layout,arrayList);
        bus = arrayList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        //final View view = inflater.inflate(R.layout.bus_single_layout,parent,false);
        final View view = inflater.inflate(R.layout.bus_extend_layout,parent,false);

        bus_no = view.findViewById(R.id.bus_no);
        final int ID = R.id.stop_0;
        for(int x=0;x<10;x++){
            stop[x] = view.findViewById(ID+x);
        }

        try {
            busList busOb = bus.get(position);
            bus_no.setText(busOb.bus_no);
            for(int i=0;i<10;i++){
                if(busOb.stops[i]!=null) {
                    Log.i(TAG, busOb.stops[i]);
                    stop[i].setText(busOb.stops[i]);
                }
                else{
                    stop[i].setVisibility(View.GONE);
                }
            }

        }catch (Exception e){
            Log.i(TAG,e.toString());
            Toast.makeText(getContext(),"Please restart this app",Toast.LENGTH_LONG).show();
        }

        return view;
    }
}















