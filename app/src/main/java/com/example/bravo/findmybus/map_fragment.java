package com.example.bravo.findmybus;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;


public class map_fragment extends Fragment implements OnMapReadyCallback {

    public static final String TAG = "FindMyBus";

    Geocoder geocoder;
    List<Address> addresses;
    GoogleMap mMap;
    String city;
    String knownName;
    String address;

    Activity mActivity;
    Bitmap bmp;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "ON create View CAlled");
        View view = inflater.inflate(R.layout.fragment_map_fragment, container, false);
        final SupportMapFragment myMAPF = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.menu_map);

        myMAPF.getMapAsync(this);
        geocoder = new Geocoder(getActivity(), Locale.getDefault());

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        bmp = Bitmap.createBitmap(80, 80, conf);
        Canvas canvas1 = new Canvas(bmp);

        // paint defines the text color, stroke width and size
        Paint color = new Paint();
        // modify canvas
        canvas1.drawBitmap(BitmapFactory.decodeResource(getResources(),
                R.drawable.location_small), 12,0, color);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        Log.i(TAG,"Map attached");
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i(TAG, "ON Maps ready called");
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.zoomBy(15.0f));
    }

    public void addMarker(double user_lat, double user_lng, final String uname){
        final LatLng user_cor = new LatLng(user_lat,user_lng);


        try {

            //address = geocoder.getFromLocationName("Nayapura ,Bhopal,Madhya Pradesh");

            addresses = geocoder.getFromLocation(user_lat, user_lng, 1);
            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            city = addresses.get(0).getLocality();
            knownName = addresses.get(0).getFeatureName();

        }catch (Exception e){
            Log.i(TAG,"EXCEPTION CATCHED");
        }
        Log.i(TAG,"address line"+address +" "+"city"+city+" "+"knownName"+" "+ knownName);


        if(mActivity!=null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMap.clear();
                    Marker marker = mMap.addMarker(new MarkerOptions()
                                    .position(user_cor)
                                    .title(uname)
                                    .snippet(address));
                    marker.showInfoWindow();
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(user_cor));
                }
            });
        }
        else {
            Toast.makeText(getActivity(),"Try restarting the app",Toast.LENGTH_LONG).show();
        }

    }

}
