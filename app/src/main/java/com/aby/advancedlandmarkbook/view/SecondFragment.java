package com.aby.advancedlandmarkbook.view;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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

import java.util.List;

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
    private SearchView mapSearchView;

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

        selectedLatitude = 0.0;
        selectedLongitude = 0.0;

        mapSearchView = binding.searchView;

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

        mapSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s)
            {
                String location = mapSearchView.getQuery().toString();
                List<Address> addressList = null;

                if (location != null)
                {
                    Geocoder geocoder = new Geocoder(getContext());

                    try
                    {
                        addressList = geocoder.getFromLocationName(location,1);
                    }

                    catch (Exception exception)
                    {
                        exception.printStackTrace();
                    }

                    Address address = addressList.get(0);
                    LatLng searchedLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(searchedLatLng).title(location));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchedLatLng, 15));
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s)
            {
                return false;
            }
        });

        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location)
            {
                    if (location != null)
                    {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
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
            if (getArguments() != null)
            {
                goToLocation();
            }

            else
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 50, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                try
                {
                    LatLng lastKnownLocationsLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastKnownLocationsLatLng, 15));
                }

                catch (NullPointerException exception)
                {
                    exception.printStackTrace();
                }
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
            if (binding.textView.getText().toString().equals(""))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Invalid location name");
                builder.setMessage("Please enter the name of location");

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        dialogInterface.cancel();
                    }
                });

                builder.show();
            }

            else
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

                    binding.textView.setVisibility(View.INVISIBLE);
                    binding.saveButton.setVisibility(View.INVISIBLE);
                    binding.searchView.setVisibility(View.INVISIBLE);

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

    @Override
    public void onResume() // hide toolbar on resume
    {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
    }

    @Override
    public void onStop() // show toolbar on stop
    {
        super.onStop();
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();
    }
}
// aktivitedeki fonksiyonu fragmenttan çağırmayı öğrendim