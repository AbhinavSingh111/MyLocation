package com.example.mylocation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

//WARNING
/* thIS APP is still buggy
so keepp the locatiion and internet connection on before opening the app.
for instance , if you close the locatioon service after opening app
then it takes it a lot of time to again be able to fetch location,
the "task" returns null

and then the image will also not be fetched since object_parsing method is called only when the "task" does not returns null.

In case you encounter theses situation ypu will need to reinstall the app.

And in case of images you may encounter several instances where the image does not loads,
plus you need be connected to the internet .


 */

/*In this app I have used FudesLocationClient to generate Latitude and longitude of a location and
Geocoder to deduce localitly ,country and address from the Latitude and Longitude*/

//Then I have used Unsplash Image api to get a picture of the locality given by Geocoder class.

/*To use the Unsplash api I have used Volley and have made a JSON Object request , The request is a
standard JSON Object request ,  using singleton class.*/

/* I have used Picasso to load the image url and set it as layout background*/

/* in uses permission I need to access the location ,required code is in manifest file*/


public class MainActivity extends AppCompatActivity {
    RequestQueue queue;
    TextView lattitude, longitude, country, locality, address;
    Button button;
    LinearLayout layout;
    ImageView imageView;

    //The Fused Location API is a higher-level Google Play Services API that wraps the underlying location sensors like GPS.
    FusedLocationProviderClient fusedLocationProviderClient;
    int random;
    String url = "https://api.unsplash.com/search/photos?query=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView7);
        lattitude = findViewById(R.id.latititude);
        longitude = findViewById(R.id.longitude);
        country = findViewById(R.id.country);
        locality = findViewById(R.id.locality);
        address = findViewById(R.id.address);
        button = findViewById(R.id.button);
        layout = findViewById(R.id.layout);

        // Get a RequestQueue
        queue = MySingleton.getInstance(this.getApplicationContext()).getRequestQueue();

        // Creating an instance of fusedlocationclient using LocationServices
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //checking the permissions
                if(ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(MainActivity.this,"Permission is given already",Toast.LENGTH_SHORT).show();

                    getLocation();

                    //Generating a random number that will be used to randomly pick an image url from Unsplash api
                    random = new Random().nextInt(10) + 0;

//                    Toast.makeText(MainActivity.this,"GetLocation Method is called",Toast.LENGTH_SHORT).show();
                }else{

                    //IF permission denied , then asking again on next opening of app
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
                }
            }

        });


    }

    private void getLocation() {

        //Getting the last location
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                // on completion of the task here we will extract the task result in Location format
                Location location = task.getResult();
//                Toast.makeText(MainActivity.this,"task result extracted",Toast.LENGTH_SHORT).show();
                //If there is a result carried by task
                if(location != null){
                    try {
                        //Here Geocoder id used to extract various parameters from the above location
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

                        // putting the latitude and latitude in a list of Address type
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);

                        //Here I am extracing the locality from the address list , this will further be used to complete the Unsplash api url
                        String city_name = addresses.get(0).getLocality();
                        city_name = city_name.replace(" ","");
                        url = url+city_name+"&client_id=pAzkCuB2GID2pCGyknu8tZLvYlxmPsVRGtAxccw9PLc";

                        imageView.setVisibility(View.GONE);

                        //Setting the results in their respective textviews
                        lattitude.setText("Latitude : "+addresses.get(0).getLatitude());
                        lattitude.setVisibility(View.VISIBLE);
                        longitude.setText("Longitude : "+addresses.get(0).getLongitude());
                        longitude.setVisibility(View.VISIBLE);
                        country.setText("Country : "+addresses.get(0).getCountryName());
                        country.setVisibility(View.VISIBLE);
                        locality.setText("Locality : "+addresses.get(0).getLocality());
                        locality.setVisibility(View.VISIBLE);
                        address.setText("Address : "+addresses.get(0).getAddressLine(0));
                        address.setVisibility(View.VISIBLE);

                        //This method will create and send the JSON object request and get the data from the api
                        object_parsing();




//                        Toast.makeText(MainActivity.this,"DOne",Toast.LENGTH_SHORT).show();


                    } catch (IOException e) {
                        e.printStackTrace();
//                        Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                }
                else
                {
                    //Im case the the task result was null
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},44);
//                    Toast.makeText(MainActivity.this,"Task result was null",Toast.LENGTH_SHORT).show();

                }
            }
        });
    }
    private void object_parsing()
    {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            Log.d("orig",url);
                            //The Unsplash api had data as an object enclosing array enclosing object fromat
                            //Here I am extracting json array from the json object returned
                            JSONArray jsonArray = response.getJSONArray("results");

                            //Here I am randomly extracting json object from json array and based on these random objects further random images will be generated
                            JSONObject jsonObject1 = jsonArray.getJSONObject(random);

                            //Here from randomly selected json object I am selesting another object named urls
                            JSONObject jsonObject2 = jsonObject1.getJSONObject("urls");

                            //From urls the url with name "regular" is being selected
                            String city_url = jsonObject2.getString("regular");

                            //Now I am using Picasso Library (whose implementation is done in gradle dependencies
                            Picasso.get().load(city_url).into(new Target() {
                                @Override
                                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                    //the image (mainactivity) is being removed
                                    imageView.setImageDrawable(null);

                                    //the pic returned from url is being set as layout background
                                    layout.setBackground(new BitmapDrawable(bitmap));


                                }

                                @Override
                                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                                    Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();

                                }

                                @Override
                                public void onPrepareLoad(Drawable placeHolderDrawable) {

                                }
                            });


                            Log.d("url",city_url);
                        } catch (JSONException e) {
                            //in case the request throws some exception
                            e.printStackTrace();
//                            Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                        }




                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
//                        Toast.makeText(MainActivity.this,error.toString(),Toast.LENGTH_LONG).show();

                    }
                });
        //adding the request to the queue
        queue.add(jsonObjectRequest);

    }


}





