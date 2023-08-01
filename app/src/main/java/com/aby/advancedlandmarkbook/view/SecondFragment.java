package com.aby.advancedlandmarkbook.view;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.aby.advancedlandmarkbook.R;
import com.aby.advancedlandmarkbook.databinding.FragmentSecondBinding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

public class SecondFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, View.OnClickListener
{
    private GoogleMap mMap;
    private FragmentSecondBinding binding;
    ActivityResultLauncher<String> permissionLauncher;
    LocationManager locationManager;
    LocationListener locationListener;
    SharedPreferences sharedPreferences;
    boolean info;
    static Double selectedLatitude;
    static Double selectedLongitude;
    Button saveButton;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    SQLiteDatabase database;

    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        // Initialize view
        binding = FragmentSecondBinding.inflate(inflater, container, false);

        database = this.getContext().openOrCreateDatabase("Locations", Context.MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE IF NOT EXISTS locations (id INTEGER PRIMARY KEY, name VARCHAR, latitude DOUBLE, longitude DOUBLE)");

        saveButton = binding.saveButton;
        saveButton.setOnClickListener(this);

        // Initialize map fragment
        SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.maps_fragment);

        // Async map
        supportMapFragment.getMapAsync(this);

        registerLauncher();

        sharedPreferences = getContext().getSharedPreferences("com.aby.advancedlandmarkbook", Context.MODE_PRIVATE);
        info = false;

        selectedLatitude = 0.0;
        selectedLongitude = 0.0;

        fragmentManager = getChildFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        binding = null;
    }

    private void goToLocation()
    {
            String name = getArguments().getString("name", "");

            System.out.println("Name: " + name);

            Cursor cursor = database.rawQuery("SELECT * FROM locations WHERE name = ?", new String[]{name});

            int latitudeIndex = cursor.getColumnIndex("latitude");
            int longitudeIndex = cursor.getColumnIndex("longitude");

            while (cursor.moveToNext())
            {
                double latitude = cursor.getDouble(latitudeIndex);
                double longitude = cursor.getDouble(longitudeIndex);
                LatLng latLng2 = new LatLng(latitude, longitude);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng2, 15));
                mMap.addMarker(new MarkerOptions().position(latLng2));
            }

            cursor.close();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap)
    {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);

        binding.saveButton.setEnabled(false);

        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location)
            {
                info = sharedPreferences.getBoolean("info", false);

                if (!info)
                {
                    if (location == null)
                    {
                        //LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                        sharedPreferences.edit().putBoolean("info", true);
                    }
                }
            }
        };

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // request permission
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION))
            {
                Snackbar.make(binding.getRoot(), "Permission needed for maps", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Give permission", new View.OnClickListener() {
                            @Override
                            public void onClick(View view)
                            {
                                // request permission
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
                            }
                        }).show();
            }

            else
            {
                // request permission
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }

        else
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 50, locationListener);

            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (getArguments() != null)
            {
                goToLocation();
            }

            else
            {
                LatLng lastKnownLocationsLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocationsLatLng, 15));
            }

            mMap.setMyLocationEnabled(true);
        }
    }

    private void registerLauncher()
    {
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result)
            {
                if (result)
                {
                    // permission granted
                    if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                    {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 50, locationListener);

                        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                        if (lastKnownLocation != null)
                        {
                            LatLng lastKnownLocationsLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocationsLatLng, 15));
                        }
                    }
                }

                else
                {
                    // permission denied
                    Toast.makeText(getContext(), "Permission needed", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng)
    {
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));

        selectedLatitude = latLng.latitude;
        selectedLongitude = latLng.longitude;

        binding.saveButton.setEnabled(true);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.saveButton)
        {
            try
            {
                String name = binding.textView.getText().toString();

                String sql = "INSERT INTO locations (name, latitude, longitude) VALUES (?, ?, ?)";

                SQLiteStatement statement = database.compileStatement(sql);
                statement.bindString(1, name);
                statement.bindDouble(2, selectedLatitude);
                statement.bindDouble(3, selectedLongitude);
                statement.execute();

                Cursor cursor = database.rawQuery("SELECT * FROM locations", null);
                int IdIndex = cursor.getColumnIndex("id");
                int nameIndex = cursor.getColumnIndex("name");
                int latitudeIndex = cursor.getColumnIndex("latitude");
                int longitudeIndex = cursor.getColumnIndex("longitude");

                while (cursor.moveToNext())
                {
                    System.out.println("Id: " + cursor.getInt(IdIndex));
                    System.out.println("Name: " + cursor.getString(nameIndex));
                    System.out.println("Latitude: " + cursor.getDouble(latitudeIndex));
                    System.out.println("Longitude: " + cursor.getDouble(longitudeIndex));
                }

                cursor.close();

                binding.textView.setVisibility(View.INVISIBLE);
                binding.saveButton.setVisibility(View.INVISIBLE);

                FirstFragment firstFragment = new FirstFragment();
                fragmentTransaction.replace(R.id.constraint_layout, firstFragment).addToBackStack(null).commit();

                Toast.makeText(getContext(), "Location saved successfully", Toast.LENGTH_LONG).show();
            }

            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }
    }
}
// aktivitedeki fonksiyonu fragmenttan çağırmayı öğrendim