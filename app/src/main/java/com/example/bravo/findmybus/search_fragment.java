package com.example.bravo.findmybus;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class search_fragment extends Fragment {

    public static final String TAG = "FindMyBus";
    map_fragment mo;
    TextView accu_text;
    TextView update_text;

    Query query;
    ValueEventListener listener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment,container,false);

        mo = (map_fragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        accu_text = view.findViewById(R.id.accu_text);
        update_text = view.findViewById(R.id.update_text);

        accu_text.setVisibility(View.GONE);
        update_text.setVisibility(View.GONE);

        return view;
    }

    public void addMarker(int i ,String uid, final String uname){

        if(query!=null && listener!=null){
            query.removeEventListener(listener);
        }

        query = FirebaseDatabase.getInstance().getReference().child("location").child(uid);
        listener = query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount()>0){
                    String lat = String.valueOf(dataSnapshot.child("latitude").getValue());
                    String lng = String.valueOf(dataSnapshot.child("longitude").getValue());
                    String accu = String.valueOf(dataSnapshot.child("accuracy").getValue());
                    String time = String.valueOf(dataSnapshot.child("time").getValue());
                    callMap(lat,lng,uname);
                    changeText(accu,time);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void addMarker(String driverKey , final String driverBusno) {
        if(query!=null && listener!=null){
            query.removeEventListener(listener);
        }

        query = FirebaseDatabase.getInstance().getReference().child("driver_location").child(driverKey);
        listener = query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount()>0){
                    String lat = String.valueOf(dataSnapshot.child("latitude").getValue());
                    String lng = String.valueOf(dataSnapshot.child("longitude").getValue());
                    String accu = String.valueOf(dataSnapshot.child("accuracy").getValue());
                    String time = String.valueOf(dataSnapshot.child("time").getValue());
                    callMap(lat,lng,driverBusno);
                    changeText(accu,time);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void changeText(String accu, String time) {
        accu_text.setVisibility(View.VISIBLE);
        update_text.setVisibility(View.VISIBLE);

        accu_text.setText("Accuracy : "+accu+" m");
        update_text.setText("Last updated : " + time);
    }

    private void callMap(String latitude, String longitude, String uname) {
        if(mo == null){
            Log.i(TAG,"OBJECT NULL");
        }
        else {
            double lat = Double.parseDouble(latitude);
            double lng = Double.parseDouble(longitude);
            mo.addMarker(lat, lng,uname);
        }
    }


}
