package com.byteshaft.itourguide;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static Button acquireLocationButton;
    Button cancelButtonDialog;
    Button okButtonDialog;
    EditText radiusEditTextOne;
    EditText radiusEditTextTwo;
    Switch enableGeofencing;
    public static ImageView imageViewName;
    LocationService locationService;
    LocationHelpers locationHelpers;
    public static MainActivity instance;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationService = LocationService.getInstance(getApplicationContext());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        enableGeofencing = (Switch) findViewById(R.id.switch_geofencing);
        instance = this;
        locationHelpers = new LocationHelpers(MainActivity.this);
        imageViewName = (ImageView) findViewById(R.id.iv_name);
        acquireLocationButton = (Button) findViewById(R.id.button_acquire_location);
        if (!locationHelpers.playServicesAvailable()) {
            locationHelpers.showGooglePlayServicesError(MainActivity.this);
        }
        acquireLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationService.mLocationChangedCounter = 0;
                acquireLocationButton.setClickable(false);
                imageViewName.setVisibility(View.GONE);
                imageViewName.setImageResource(R.mipmap.name_aquiring_location);
                locationHelpers = new LocationHelpers(getApplicationContext());
                locationService.connectingGoogleApiClient();
                if (locationHelpers.playServicesAvailable() && !locationHelpers.isAnyLocationServiceAvailable()) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    alertDialog.setTitle("Location Service disabled");
                    alertDialog.setMessage("Want to enable?");
                    alertDialog.setCancelable(false);
                    alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    });
                    alertDialog.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            System.exit(0);
                        }
                    });
                    alertDialog.show();
                } else if (!locationHelpers.playServicesAvailable()) {
                    locationHelpers.showGooglePlayServicesError(MainActivity.this);
                } else {
                    locationService.locationTimer().start();
                }
            }
        });
        enableGeofencing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Log.i("Switch", "ON");
                } else {
                    Log.i("Switch", "OFF ");
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {

            final Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.activity_settings_dialog);
            dialog.setCancelable(false);

            radiusEditTextOne = (EditText) dialog.findViewById(R.id.et_radius_one);
            radiusEditTextOne.setText(String.format("%d", sharedPreferences.getInt("radius_one", 10)));
            radiusEditTextTwo = (EditText) dialog.findViewById(R.id.et_radius_two);
            radiusEditTextTwo.setText(String.format("%d", sharedPreferences.getInt("radius_two", 2)));

            cancelButtonDialog = (Button) dialog.findViewById(R.id.button_dialog_cancel);
            cancelButtonDialog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            okButtonDialog = (Button) dialog.findViewById(R.id.button_dialog_ok);
            okButtonDialog.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (radiusEditTextOne.getText().toString().trim().length() < 1
                            || radiusEditTextTwo.getText().toString().trim().length() < 1) {
                        Toast.makeText(getApplicationContext(), "One or more fields are empty", Toast.LENGTH_SHORT).show();
                    } else {
                        int radiusOne = Integer.parseInt(radiusEditTextOne.getText().toString().trim());
                        sharedPreferences.edit().putInt("radius_one", radiusOne).apply();

                        int radiusTwo = Integer.parseInt(radiusEditTextTwo.getText().toString().trim());
                        sharedPreferences.edit().putInt("radius_two", radiusTwo).apply();

                        dialog.dismiss();
                    }
                }
            });

            dialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (locationService.mGoogleApiClient.isConnected()) {
            locationService.stopLocationService();
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();}
}
