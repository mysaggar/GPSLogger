package com.example.gpslogger;


import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener{

    private Location location;
    private TextView latitudeText;
    private TextView longitudeText;
    private TextView speedText;
    private Button startButton;
    private Button stopButton;
    private GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;
    private static final long UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000;

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();

    private static final int ALL_PERMISSIONS_RESULT = 1011;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitudeText = findViewById(R.id.latValue);
        longitudeText = findViewById(R.id.longValue);
        speedText = findViewById(R.id.speedValue);
        startButton = findViewById(R.id.start);
        stopButton = findViewById(R.id.stop);

        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.INTERNET);
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        permissionsToRequest = permissionsToRequest(permissions);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if(permissionsToRequest.size() > 0){
                requestPermissions(permissionsToRequest.
                        toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            }
        }

        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();

    }

    private ArrayList<String> permissionsToRequest(ArrayList<String> wantedPermissions) {
        ArrayList<String> result = new ArrayList<>();
        for (String perm : wantedPermissions) {
            if(!hasPermission(perm)) {
                result.add(perm);
            }
        }
        return result;
    }

    private boolean hasPermission(String permission) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(googleApiClient != null)
        {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!checkPlayServices()){
            Toast.makeText(this, "Install Google Play Services", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(googleApiClient !=null && googleApiClient.isConnected()){
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, (com.google.android.gms.location.LocationListener) this);
            googleApiClient.disconnect();
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if(resultCode != ConnectionResult.SUCCESS) {
            if(apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this,resultCode,PLAY_SERVICES_RESOLUTION_REQUEST);
            }
            else {
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if(location != null)
        {
            String latitude = String.format(Locale.ENGLISH, "%f", location.getLatitude());
            String longitude = String.format(Locale.ENGLISH,"%f", location.getLongitude());
            String speed = String.format(Locale.ENGLISH,"%f", location.getSpeed());
            latitudeText.setText(latitude);
            longitudeText.setText(longitude);
            speedText.setText(speed);
        }

        startLocationUpdates();


    }

    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority((LocationRequest.PRIORITY_HIGH_ACCURACY));
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this,"You need to enable permissions.display location!",Toast.LENGTH_SHORT).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest, (com.google.android.gms.location.LocationListener) this);

    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null)
        {
            String latitude = String.format(Locale.ENGLISH, "%f", location.getLatitude());
            String longitude = String.format(Locale.ENGLISH,"%f", location.getLongitude());
            String speed = String.format(Locale.ENGLISH,"%f", location.getSpeed());
            latitudeText.setText(latitude);
            longitudeText.setText(longitude);
            speedText.setText(speed);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                for(String perm: permissionsToRequest) {
                    if(!hasPermission(perm)){
                        permissionRejected.add(perm);
                    }
                }
                if(permissionRejected.size()>0){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    {
                        if(shouldShowRequestPermissionRationale(permissionRejected.get(0))) {
                            new AlertDialog.Builder(MainActivity.this).
                                    setMessage("These permissions are mandatory to get your location.You need to allow them.").
                                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                requestPermissions(permissionRejected.toArray(new String[permissionRejected.size()]),
                                                        ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    }).
                                    setNegativeButton("Cancel", null).create().show();
                            return;
                        }
                    }
                }
                else if(googleApiClient != null) {
                    googleApiClient.connect();
                }
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void startLogging(View view) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
/*public class MainActivity extends AppCompatActivity implements LocationListener{

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    Button startButton;
    Button stopButton;
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
    private ArrayList<String> permissionsToRequest;

    private ArrayList<String> permissionsToRequest(String[] wantedPermissions) {
        ArrayList<String> result = new ArrayList<>();

        for (String perm : wantedPermissions) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.start);
        stopButton = findViewById(R.id.stop);
        latitudeText = findViewById(R.id.latValue);
        longitudeText = findViewById(R.id.longValue);
        speedText = findViewById(R.id.speedValue);

        permissionsToRequest = permissionsToRequest(PERMISSIONS);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0) {
                requestPermissions(permissionsToRequest.
                        toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            }
        }

        *//*startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation();
            }
        });*//*

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


}*/
