package com.example.bravo.findmybus;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;


public class user_fragment extends Fragment {

    public static final String TAG = "FindMyBus";
    private Query query;
    private Query bus_query;
    private ValueEventListener listener;
    private ValueEventListener bus_listener;


    private EditText search_data;
    private ImageView search_img;
    private ImageView phone_img;
    private TextView driver_location;
    private String lc;
    private String driverKey;
    private String driverBusNo;
    private String phoneNo;
    private ScrollView scrollView;
    private LinearLayout driver_layout;
    private ProgressDialog progressDialog;

    private ListView listView;
    private ArrayList<userList> arrayList = new ArrayList<>();
    private ListAdapter listAdapter;


    TextView search_text;
    ImageView user_img;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.user_fragment,container,false);

        final main_menu menuOb = (main_menu) getActivity();
        //search_fragment searchOb = (search_fragment) getFragmentManager().findFragmentById();

        search_data = view.findViewById(R.id.search_data);
        search_img = view.findViewById(R.id.search_img);
        phone_img = view.findViewById(R.id.phone_img);
        driver_location = view.findViewById(R.id.driver_location);

        listView = view.findViewById(R.id.user_list);
        scrollView = view.findViewById(R.id.scroll_view);
        driver_layout = view.findViewById(R.id.driver_layout);

        final InputMethodManager imm =  (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);


        search_text = view.findViewById(R.id.search_text);
        user_img = view.findViewById(R.id.user_img);
        Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/UnicaOne-Regular.ttf");
        search_data.setTypeface(custom_font);
        search_text.setTypeface(custom_font);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Checking..");
        progressDialog.setMessage("please wait this could take a while...");
        progressDialog.setCancelable(false);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                userList uo = (userList) adapterView.getItemAtPosition(position);
                if(Objects.equals(uo.location, "1")) {
                    //Toast.makeText(getContext(), uo.uname, Toast.LENGTH_SHORT).show();
                    menuOb.setView(1,uo.uid,uo.uname);
                }
                else{
                    Toast.makeText(getContext(), "Location currently unavailabe.", Toast.LENGTH_SHORT).show();
                }
            }
        });


        search_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(),0);
                String bus_no = search_data.getText().toString();
                if(!Objects.equals(bus_no, "")){
                    arrayList.clear();
                    if (query != null && listener != null) {
                        query.removeEventListener(listener);
                    }
                    if(bus_query != null && bus_listener != null){
                        bus_query.removeEventListener(bus_listener);
                    }
                    server(bus_no);
                }
            }
        });

        phone_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNo));
                startActivity(intent);
            }
        });

        driver_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Objects.equals(lc, "0"))
                    Toast.makeText(getContext(), "Location Currently Not Available", Toast.LENGTH_SHORT).show();
                else{
                    menuOb.setView(driverKey,driverBusNo);
                }
            }
        });


        return view;
    }

    private void server(String bus_no) {
        progressDialog.show();
        query = FirebaseDatabase.getInstance().getReference().child("users").orderByChild("bus").equalTo(bus_no);
        bus_query = FirebaseDatabase.getInstance().getReference().child("drivers").orderByChild("bus").equalTo(bus_no);

        listener = query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                arrayList.clear();
                if(dataSnapshot.getChildrenCount()>0 ){
                    for(DataSnapshot child : dataSnapshot.getChildren() ){
                        String uid = child.getKey();
                        String uname = String.valueOf(child.child("username").getValue());
                        String location = String.valueOf(child.child("location").getValue());
                        String avatar = String.valueOf(child.child("avatar").getValue());

                        userList temp_obj = new userList(uid,uname,location,avatar);
                        arrayList.add(temp_obj);

                    }
                    user_img.setVisibility(View.GONE);
                    search_text.setVisibility(View.GONE);
                    scrollView.setVisibility(View.VISIBLE);
                    try {
                        listAdapter = new searchRowAdapter(getContext(), arrayList);
                    }
                    catch (Exception e){
                        Log.i(TAG,"Restart needed");
                    }
                    listView.setAdapter(listAdapter);
                    Log.i(TAG,"Result not null:"+dataSnapshot.getChildrenCount());
                }
                else{
                    listView.setAdapter(null);
                    user_img.setVisibility(View.VISIBLE);
                    search_text.setText("No Result :(");
                    search_text.setVisibility(View.VISIBLE);
                    scrollView.setVisibility(View.GONE);
                    Log.i(TAG,"Result is NUll:"+dataSnapshot.getChildrenCount());
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        bus_listener = bus_query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount()>0){
                    driver_layout.setVisibility(View.VISIBLE);
                    for(DataSnapshot child : dataSnapshot.getChildren() ){
                        lc = String.valueOf(child.child("location").getValue());
                        driverKey = child.getKey();
                        driverBusNo = "Bus No." + String.valueOf(child.child("bus").getValue());
                        phoneNo = String.valueOf(child.child("phone no").getValue());
                        Log.i(TAG,"Driver Location:"+lc);
                        if (Objects.equals(lc, "1")) {
                            driver_location.setText("Location Available");
                            driver_location.setTextColor(Color.parseColor("#3F51B5"));
                        } else {
                            driver_location.setText("Location Not Available");
                            driver_location.setTextColor(Color.parseColor("#E91E63"));
                        }
                    }

                    //Boolean lc = (Boolean) dataSnapshot.child("location").getValue();

                }
                else{
                    driver_layout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


}
















