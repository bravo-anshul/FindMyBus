package com.example.bravo.findmybus;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class direction_fragment extends Fragment {

    public static final String TAG = "FindMyBus";
    public int count=1;
    ImageView direction_imgg;
    ImageView search_img;
    Button fetch_btn;
    EditText bus_data;
    ProgressDialog progressDialog;

    private ListView listView;
    private ArrayList<busList> arrayList = new ArrayList<>();
    private ListAdapter listAdapter;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.direction_fragment,container,false);

        direction_imgg = view.findViewById(R.id.direction_image);
        search_img = view.findViewById(R.id.search_img_dir);
        fetch_btn = view.findViewById(R.id.fetch_btn);
        bus_data = view.findViewById(R.id.bus_data);

        listView = view.findViewById(R.id.bus_list);

        final InputMethodManager imm =  (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        Typeface custom_font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/UnicaOne-Regular.ttf");
        bus_data.setTypeface(custom_font);
        fetch_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetch("all");
            }
        });
        search_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imm.hideSoftInputFromWindow(getView().getWindowToken(),0);
                String s = bus_data.getText().toString();
                fetch(s);
            }
        });

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setTitle("Checking..");
        progressDialog.setMessage("please wait this could take a while...");
        progressDialog.setCancelable(false);


        return view;
    }

    public void fetch(String s){
        if(Objects.equals(s, "")){
            Toast.makeText(getContext(),"Please fill the destination field",Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.show();
        direction_imgg.setVisibility(View.GONE);
        fetch_btn.setVisibility(View.GONE);

        Query query;

        if(s.equals("all")){
            query = FirebaseDatabase.getInstance().getReference().child("routes_data");
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getChildrenCount()>0){
                        for(DataSnapshot child : dataSnapshot.getChildren() ){
                            String bus_no = String.valueOf(child.getKey());
                            busList tempObj = new busList(bus_no);
                            int x = (int) child.getChildrenCount();
                            for(int j=1;j<=x;j++){
                                String s = String.valueOf(child.child("stop_"+j).getValue());
                                tempObj.stops[j-1] = s;
                            }

                            arrayList.add(tempObj);
                        }
                        direction_imgg.setVisibility(View.GONE);
                        fetch_btn.setVisibility(View.GONE);
                        listAdapter = new busRowAdapter(getContext(),arrayList);
                        listView.setAdapter(listAdapter);
                        progressDialog.dismiss();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }else{
            arrayList.clear();
            s = upperCase(s);
            for(int i=1;i<10;i++,count++){
                query = FirebaseDatabase.getInstance().getReference().child("routes_data").orderByChild("stop_"+i).startAt(s)
                        .endAt(s+"\uf8ff");

                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getChildrenCount()>0){
                            for(DataSnapshot child : dataSnapshot.getChildren() ){
                                String bus_no = String.valueOf(child.getKey());
                                busList tempObj = new busList(bus_no);
                                int x = (int) child.getChildrenCount();
                                for(int j=1;j<=x;j++){
                                    String s = String.valueOf(child.child("stop_"+j).getValue());
                                    tempObj.stops[j-1] = s;
                                }

                                arrayList.add(tempObj);
                            }

                        }

                        if(arrayList.isEmpty()){
                            direction_imgg.setVisibility(View.VISIBLE);
                            fetch_btn.setVisibility(View.VISIBLE);
                            listView.setAdapter(null);
                        }
                        else{
                            direction_imgg.setVisibility(View.GONE);
                            fetch_btn.setVisibility(View.GONE);
                            listAdapter = new busRowAdapter(getContext(),arrayList);
                            listView.setAdapter(listAdapter);
                        }
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                });
            }

        }


    }

    public static String upperCase(String givenString) {
        String[] arr = givenString.split(" ");
        StringBuffer sb = new StringBuffer();

        for (String anArr : arr) {
            sb.append(Character.toUpperCase(anArr.charAt(0)))
                    .append(anArr.substring(1)).append(" ");
        }
        return sb.toString().trim();
    }

}










