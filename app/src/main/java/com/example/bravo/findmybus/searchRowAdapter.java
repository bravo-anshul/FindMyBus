package com.example.bravo.findmybus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class searchRowAdapter extends ArrayAdapter<userList> {

    public static final String TAG = "FindMyBus";

    private ArrayList<userList> user;
    private ImageView avatar_img;
    private TextView uname_text;
    private TextView location_text;


    public searchRowAdapter(Context context, ArrayList<userList> userLists) {
        super(context,R.layout.user_single_layout,userLists);
        user = userLists;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        final View view = inflater.inflate(R.layout.user_single_layout,parent,false);

        avatar_img = view.findViewById(R.id.avatar_img);
        uname_text = view.findViewById(R.id.uname_text);
        location_text = view.findViewById(R.id.location_text);

        userList uob = user.get(position) ;
        String uname = uob.uname;
        final String uid = uob.uid;
        int location;
        try {
            location = Integer.parseInt(uob.location);
        }catch (Exception e){
            Log.i(TAG, String.valueOf(e));
            location=0;
        }
        if(uob.avatar.length()>5) {
            String url = uob.avatar;
            new ImageLoadTask(url, avatar_img).execute();
        }
        else{
            int av = Integer.parseInt(uob.avatar);
            int resId = getContext().getResources().getIdentifier("avatar_"+av, "drawable", getContext().getPackageName());
            avatar_img.setImageResource(resId);
        }

        final ImageView noti_img = view.findViewById(R.id.noti_img);
        uname_text.setText(uname);

        if(location == 0){
            location_text.setTextColor(Color.parseColor("#E91E63"));
            location_text.setText("User Not Sharing Location");
        } else {
            noti_img.setVisibility(View.GONE);
            location_text.setTextColor(Color.parseColor("#3F51B5"));
            location_text.setText("Location Available");
        }

        noti_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("notifications").child(uid).push();
                database.setValue(uid).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            int resId = getContext().getResources().getIdentifier("bell_3", "drawable", getContext().getPackageName());
                            final ImageView noti_img = view.findViewById(R.id.noti_img);
                            noti_img.setImageResource(resId);
                            noti_img.setEnabled(false);
                        }
                        else{
                            Toast.makeText(getContext(),"Notification sending failed",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        return view;
    }

    public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

        private String url;
        private ImageView imageView;

        public ImageLoadTask(String url, ImageView imageView) {
            this.url = url;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            try {
                URL urlConnection = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) urlConnection
                        .openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imageView.setImageBitmap(result);
        }

    }
}
