package edu.sjsu.anomalydetection;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    public DynamoDBMapper dynamoDBMapper;
    List<UserLocationDO> userlatLong;
    String[] todaydateArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
        @Override
        public void onComplete(AWSStartupResult awsStartupResult) {
            AmazonDynamoDBClient dynamoDBClient = new AmazonDynamoDBClient(AWSMobileClient.getInstance().getCredentialsProvider());
            dynamoDBMapper = DynamoDBMapper.builder()
                    .dynamoDBClient(dynamoDBClient)
                    .awsConfiguration(
                            AWSMobileClient.getInstance().getConfiguration())
                    .build();
            Log.i("dynamo", String.valueOf(dynamoDBMapper));
            FetchLocations();
        }
         }).execute();

        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(10);
        googleMap.moveCamera(zoom);
        googleMap.animateCamera(zoom);
        try {
            googleMap.setMyLocationEnabled(true);

        } catch (SecurityException se) {

        }
        googleMap.setTrafficEnabled(true);
        googleMap.setIndoorEnabled(true);
        googleMap.setBuildingsEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);


        for(int i = 0; i< userlatLong.size(); i++) {
            LatLng tempLoc = new LatLng(Double.parseDouble(userlatLong.get(i).getLatitude()), Double.parseDouble(userlatLong.get(i).getLongitude()));
            if(Double.parseDouble(userlatLong.get(i).getAnomalyScore())<1.0)
                googleMap.addMarker(new MarkerOptions().position(tempLoc).title(userlatLong.get(i).getDateTime()));
            else
                googleMap.addMarker(new MarkerOptions().position(tempLoc).title(userlatLong.get(i).getDateTime()).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(tempLoc));
        }
    }

    public void FetchLocations() {
        String currentDate= String.valueOf(Calendar.getInstance().getTime());
        todaydateArray=currentDate.split(" ");

        Thread thrFetchLoc =  new Thread(new Runnable() {
            @Override
            public int hashCode() {
                return super.hashCode();
            }

            @Override
            public void run() {
                Map<String, AttributeValue> userVal = new HashMap<>();
                userVal.put(":Patient_UserName", new AttributeValue().withS("nidhi.jmr@gmail.com"));
                //userValues.put(":Datetym", new AttributeValue().withS("Mon Apr 09 20:03:16 PDT 2018"));
                //userVal.put(":Datetym", new AttributeValue().withS(todaydateArray[0]+ " "+ todaydateArray[1] + " " + todaydateArray[2]));
                userVal.put(":Datetym", new AttributeValue().withS("Mon Apr 09 "));

                Map<String, String> userVal2 = new HashMap<>();
                userVal2.put("#Dt", "DateTime");
                DynamoDBScanExpression scanExpression = new DynamoDBScanExpression()
                        .withFilterExpression("Patient_UserName = :Patient_UserName and contains(#Dt, :Datetym)").withExpressionAttributeValues(userVal).withExpressionAttributeNames(userVal2);

                try {
                    userlatLong = dynamoDBMapper.scan(UserLocationDO.class, scanExpression);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                if (userlatLong.isEmpty()) {
                }
                Log.i("****Data Fetched****", String.valueOf(userlatLong.size()));
            }
        });
        thrFetchLoc.start();

        try {
            thrFetchLoc.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
