package edu.sjsu.anomalydetection;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.kinesis.kinesisrecorder.KinesisRecorder;
import com.amazonaws.regions.Regions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;


import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.UUID;

import static java.lang.Thread.sleep;

public class MyService extends Service implements ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public MyService() {
    }

    private Context ctx;
    public static final String SEND_LOCATION_INTENT = "edu.sjsu.anomalydetection.sendLocation";
    private FusedLocationProviderClient fusedLocClient;
    private LocationRequest locReq;
    private GoogleApiClient ggleAPICli;
    private Location lastLoc;
    JSONObject userLocData = null;
    private KinesisRecorder recorder;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        ctx = getApplicationContext();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return flags;

            getGgleAPIClient();
            getLocRequest();

        fusedLocClient = LocationServices.getFusedLocationProviderClient(this);

        if(ggleAPICli!=null)
            ggleAPICli.connect();

        //kinesis
//        String kinesisDirectory = "Alzm-kinesis";
//        recorder = new KinesisRecorder(
//                this.getDir(kinesisDirectory, 0),
//                Regions.US_EAST_1,
//                AWSMobileClient.getInstance().getCredentialsProvider()
//        );
        return super.onStartCommand(intent, flags, startId);
    }

    private void getLocRequest() {
        locReq = new LocationRequest();
        locReq.setInterval(5000);
        locReq.setFastestInterval(4000);
        locReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locReq.setSmallestDisplacement(10);
    }

    private void getGgleAPIClient() {
        ggleAPICli = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        ggleAPICli.connect();

    }

    private void getLocUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(ggleAPICli, locReq, this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        showLocation();
        getLocUpdates();
    }

    private void showLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        lastLoc = LocationServices.FusedLocationApi.getLastLocation(ggleAPICli);

        if (lastLoc != null) {
            Log.i("LastLoc", "location fetched");
            double latitude = lastLoc.getLatitude();
            double longitude = lastLoc.getLongitude();

            userLocData = new JSONObject();
            try {
                userLocData.put("Latitude", latitude);
                userLocData.put("Longitude", longitude);
                userLocData.put("DateTime", Calendar.getInstance().getTime());
                userLocData.put("ID", UUID.randomUUID().toString());
                userLocData.put("Patient_UserName", LoginActivity.getLoggedINUser());

            } catch (JSONException e) {
                Log.i("JSONException", "JSONException");
                e.printStackTrace();
            }

            Log.i("JSON in MyService: ",userLocData.toString());

            Intent i = new Intent();
            i.setAction(SEND_LOCATION_INTENT);

            i.putExtra("location", latitude + "/" + longitude);
            ctx.sendBroadcast(i);
            //  kinesis
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    recorder.saveRecord(userLocData.toString(),"input-alzm-stream");
//                    recorder.submitAllRecords();
//                }
//            }).start();

        } else {

            Intent i = new Intent();
            i.setAction(SEND_LOCATION_INTENT);
            i.putExtra("location", "null");
            ctx.sendBroadcast(i);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        ggleAPICli.connect();
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastLoc=location;
        showLocation();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if(ggleAPICli!=null)
            ggleAPICli.connect();
    }

    @Override
    public boolean stopService(Intent name) {
        LocationServices.FusedLocationApi.removeLocationUpdates(ggleAPICli, this);
        if(ggleAPICli!=null)
            ggleAPICli.disconnect();
        return super.stopService(name);

    }


}
