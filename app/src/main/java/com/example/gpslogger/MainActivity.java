package com.example.gpslogger;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.opencsv.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener{

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    Button startLoggingBtn;
    Button stopLoggingBtn;
    TextView latitudeText;
    TextView longitudeText;
    TextView speedText;

    LocationManager locationManager;
    String provider;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.INTERNET
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startLoggingBtn = findViewById(R.id.start);
        stopLoggingBtn = findViewById(R.id.stop);
        latitudeText = findViewById(R.id.latValue);
        longitudeText = findViewById(R.id.longValue);
        speedText = findViewById(R.id.speedValue);

        /*startLoggingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });*/

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        provider = locationManager.getBestProvider(new Criteria(), false);
    }

    void getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, this);
        }
        catch(SecurityException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        String latitude = String.format(Locale.ENGLISH, "%f", location.getLatitude());
        String longitude = String.format(Locale.ENGLISH,"%f", location.getLongitude());
        String speed = String.format(Locale.ENGLISH,"%f", location.getSpeed());
        latitudeText.setText(""+location.getLatitude());
        longitudeText.setText(longitude);
        speedText.setText(speed);
        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "GPSLogger";
        File dir = new File(baseDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = "Analysis.csv";
        String filePath = dir + File.separator + fileName;
        File f = new File(filePath);
        CSVWriter writer = null;
        FileWriter mFileWriter;
        if (f.exists() && !f.isDirectory()) {
            try {
                mFileWriter = new FileWriter(filePath, true);
                writer = new CSVWriter(mFileWriter);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            try {
                writer = new CSVWriter(new FileWriter(filePath));
                String[] data = {"Latitude", "Longitude", "Speed"};
                writer.writeNext(data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String[] data = {latitude, longitude, speed};

        try {
            writer.writeNext(data);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(MainActivity.this, "Please Enable GPS and Internet", Toast.LENGTH_SHORT).show();
    }

    public void onViewMap(View view) {
        Intent intent = new Intent(MainActivity.this, gpsMarker.class);
        startActivity(intent);
    }



    public void startLogging(View view) {
        getLocation();
    }

    public void stopLogging(View view) {
        locationManager.removeUpdates(this);
        latitudeText.setText("0.0");
        longitudeText.setText("0.0");
        speedText.setText("0.0");

        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "GPSLogger";
        File dir = new File(baseDir);
        if(!dir.exists()){
            dir.mkdirs();
        }
        String fileName = "Analysis.csv";
        String filePath = dir + File.separator + fileName;
        String message = "File stored at " + filePath;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


}
