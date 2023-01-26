package com.mustaar.route;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.gms.tasks.Task;

import com.mustaar.route.SQLConnection.ConnectionClass;
import com.mustaar.route.Session.SessionClass;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    CountDownTimer timer;

    LocationRequest locationRequest;
    public static final int REQUEST_CHECK_SETTING=1001;
    FusedLocationProviderClient fusedLocationProviderClient;
    Connection con;
    Button logoutButton;
    SessionClass sessionManager;
    HashMap<String,String> userDetails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timer=null;
        sessionManager = new SessionClass(getApplicationContext());
        sessionManager.checkLogin();

            if(timer!=null)
                timer.cancel();
        userDetails=sessionManager.getUserDetails();
        logoutButton=(Button)findViewById(R.id.logoutbtn);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sessionManager.LogoutUser();
                finish();
            }
        });
        // initialize fusedLocationProviderClient
        fusedLocationProviderClient= LocationServices.getFusedLocationProviderClient(this);
        if(sessionManager.isLoggedIn()) {
            getLocation();
            runTimer();
        }
    }

    @Override
    protected void onDestroy() {
        if(timer!=null)
            timer.cancel();
        super.onDestroy();
    }

    protected void runTimer()
    {
        if(sessionManager.isLoggedIn()) {
            timer = new CountDownTimer(900000, 5000) {  //900000 ms = 15 minutes

                @Override
                public void onTick(long l) {
                    Log.d("Debug", "Remaining sec: " + l / 1000);
                }

                @Override
                public void onFinish() {
                    getLocation();
                    runTimer();
                }

            }.start();
        }
    }

    protected void logLocation(Address address)
    {
        Log.d("Debug", "Latitude: " + address.getLatitude());
        Log.d("Debug", "Longitude: " + address.getLongitude());
        con=connectionClass(ConnectionClass.ip,ConnectionClass.port,ConnectionClass.username,ConnectionClass.password,ConnectionClass.db);
        try{
            if(con!=null)
            {
                Log.d("Debug", "Latitude: " + address.getLatitude());
                Log.d("Debug", "Longitude: " + address.getLongitude());
                String insert="INSERT INTO Log VALUES ('"+userDetails.get("phone")+"', GETDATE(), GETDATE(), " + String.valueOf(address.getLatitude())+", "+ String.valueOf(address.getLongitude()) +")";
                Statement s = con.createStatement();
                s.execute(insert);
                con.close();
            }

        }
        catch (Exception exception){
            Log.e("Error",exception.getMessage());
            Log.d("Cause", userDetails.get("phone"));
        }
    }

    // requests location permission if not allowed
    // then gets
    @SuppressLint("MissingPermission")
    private void getLocation() {


        if(ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
            Log.e("Debug", "Here1");
            fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
                @NonNull
                @Override
                public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                    return null;
                }
                @Override
                public boolean isCancellationRequested() {
                    return false;
                }
            }).addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    statusCheck();
                    // Initialize Location
                    Location location = task.getResult();
                    if (location != null) {
                        try{
                            Log.e("Debug", "Here2");
                            //initialize geocoder
                            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                            // iniitalize address list
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            // Log location called
                            logLocation(addresses.get(0));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    else
                        Log.e("Debug", "Here3");
                }
            });
        }
        else
        {
            Log.e("Debug", "Here");
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    @SuppressLint("NewApi")
    public Connection connectionClass(String ip, String port, String un, String pwd, String db) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().build();
        StrictMode.setThreadPolicy(policy);
        Connection connection = null;
        String url = null;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            url="jdbc:jtds:sqlserver://"+ip+":"+port+";databaseName="+db+";user="+un+";password="+pwd+";";
            connection= DriverManager.getConnection(url);

        } catch (Exception e) {
            Log.e("Sql Connection Error", e.getMessage());
            Log.e("Cause at Login", String.valueOf(e.getCause()));
        }
        return connection;
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                        buildAlertMessageNoGps();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}