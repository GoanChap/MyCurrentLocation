package app.kidharhai.com.mycurrentlocation.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.net.Uri;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.support.design.widget.Snackbar;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import app.kidharhai.com.mycurrentlocation.BuildConfig;
import app.kidharhai.com.mycurrentlocation.ModelPackage.Model;
import app.kidharhai.com.mycurrentlocation.R;


public class MainActivity extends AppCompatActivity {

    Button goButton;
    Button mapsButton;
    Button shareButton;
    Button browserButton;
    ProgressBar progressBar;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_CODE = 1000;
    private static String lat ="";
    private static String lng="";

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    MenuItem item;
    Model model = new Model();


    @Override
    public void onStart() {
        super.onStart();

        if (!checkPermissions()) {
            requestPermissions();

        } else {
//            getLastLocation();
            shareButton.setEnabled(false);
            mapsButton.setEnabled(false);
            browserButton.setEnabled(false);
            progressBar.setVisibility(View.INVISIBLE);

        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length <= 0) {
                    // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i(TAG, "User interaction was cancelled.");
                } else
                 {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        // Permission denied.

                        // Notify the user via a SnackBar that they have rejected a core permission for the
                        // app, which makes the Activity useless. In a real app, core permissions would
                        // typically be best requested during a welcome-screen flow.

                        // Additionally, it is important to remember that a permission might have been
                        // rejected without asking the user for permission (device policy or "Never ask
                        // again" prompts). Therefore, a user interface affordance is typically implemented
                        // when permissions are denied. Otherwise, your app could appear unresponsive to
                        // touches or interactions which have required permissions.
                        showSnackbar(R.string.textwarn, R.string.settings,
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        // Build intent that displays the App settings screen.
                                        Intent intent = new Intent();
                                        intent.setAction(
                                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        Uri uri = Uri.fromParts("package",
                                                BuildConfig.APPLICATION_ID, null);
                                        intent.setData(uri);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                    }
                                });
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        goButton = findViewById(R.id.getLocationButtonID);
        mapsButton = findViewById(R.id.mapButtonID);
        shareButton = findViewById(R.id.shareButtonID);
        browserButton = findViewById(R.id.browserButtonID);
        progressBar = findViewById(R.id.progressBarID);

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        } else {

            //If permission is granted
            buildLocationRequest();
            buildLocationCallback();

            //Create FusedProviderClient
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        }


        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);

                    return;

                }
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,Looper.myLooper());

                progressBar.setVisibility(View.VISIBLE);
               shareButton.setEnabled(true);
                mapsButton.setEnabled(true);
                browserButton.setEnabled(true);

            }


        });

        browserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);

                    return;

                }
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,Looper.myLooper());


                lat = model.getMyLatitude();
                lng = model.getMyLongitude();
                String url = "https://www.google.com/search?q=" + lat + "," + lng ;

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);


            }
        });

        mapsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);

                    return;

                }
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,Looper.myLooper());


                lat = model.getMyLatitude();
                lng = model.getMyLongitude();
                String url = "https://www.google.com/maps/@" + lat + "," + lng + ",19z";

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);


            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);

                    return;

                }
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,Looper.myLooper());


                lat = model.getMyLatitude();
                lng = model.getMyLongitude();
                String url = "https://www.google.com/search?q=" + lat + "," + lng ;
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                sendIntent.setType("text/plain");

                Intent chooser = Intent.createChooser(sendIntent, "Share with friends:");
                if(sendIntent.resolveActivity(getPackageManager()) != null){
                    startActivity(chooser);
                }


            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.about, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_settings:
                launchAboutMe();

                break;
        }
        return super.onOptionsItemSelected(item);

    }

    private void launchAboutMe() {

        Intent intent = new Intent(this, ActivityAboutMe.class);
        startActivity(intent);
    }

        private void buildLocationCallback() {

        locationCallback = new LocationCallback()
        {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                for(Location location:locationResult.getLocations()) {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), String.valueOf(location.getLatitude())
                            + "/" + String.valueOf(location.getLongitude()), Toast.LENGTH_LONG).show();
                    model.setMyLatitude(String.valueOf(location.getLatitude()));
                    model.setMyLongitude(String.valueOf(location.getLongitude()));
                    stop();
                }

            }
        };

        }

    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_CODE);
    }

    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");

            showSnackbar(R.string.textwarn, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            startLocationPermissionRequest();
                        }
                    });

        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            startLocationPermissionRequest();
        }
    }

    private void buildLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10);
    }

    public void stop(){
        Log.i(TAG, "stop() Stop location tracking");
        this.fusedLocationProviderClient.removeLocationUpdates(this.locationCallback);
    }

}
