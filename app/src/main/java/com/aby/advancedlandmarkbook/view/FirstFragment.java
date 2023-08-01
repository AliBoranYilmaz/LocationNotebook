package com.aby.advancedlandmarkbook.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.aby.advancedlandmarkbook.R;
import com.aby.advancedlandmarkbook.adapter.RecyclerViewAdapter;
import com.aby.advancedlandmarkbook.databinding.FragmentFirstBinding;
import com.aby.advancedlandmarkbook.model.Location;

import java.util.ArrayList;

public class FirstFragment extends Fragment implements RecyclerViewInterface
{
    private FragmentFirstBinding binding;
    ArrayList<Location> locationArrayList;
    RecyclerViewAdapter adapter;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    SQLiteDatabase database;

    public FirstFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        database = this.getContext().openOrCreateDatabase("Locations", Context.MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE IF NOT EXISTS locations (id INTEGER PRIMARY KEY, name VARCHAR, latitude DOUBLE, longitude DOUBLE)");

        fragmentManager = getChildFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        binding = FragmentFirstBinding.inflate(getLayoutInflater(), container, false);

        locationArrayList = new ArrayList<>();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecyclerViewAdapter(locationArrayList, this);
        binding.recyclerView.setAdapter(adapter);

        getData();

        return binding.getRoot();
    }

    private void getData()
    {
        try
        {
            Cursor cursor = database.rawQuery("SELECT * FROM locations", null);

            int nameIndex = cursor.getColumnIndex("name");
            int IdIndex = cursor.getColumnIndex("id");
            int latitudeIndex = cursor.getColumnIndex("latitude");
            int longitudeIndex = cursor.getColumnIndex("longitude");

            while (cursor.moveToNext())
            {
                String name = cursor.getString(nameIndex);
                int id = cursor.getInt(IdIndex);
                int latitude = cursor.getInt(latitudeIndex);
                int longitude = cursor.getInt(longitudeIndex);
                Location location = new Location(id, name, latitude, longitude);

                locationArrayList.add(location);
            }

            adapter.notifyDataSetChanged();

            cursor.close();
        }

        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    @Override
    public void onItemClick(int position) // RecyclerViewInterface method
    {
        Location location = locationArrayList.get(position);
        Bundle args = new Bundle();
        args.putString("name", location.name);

        SecondFragment secondFragment = new SecondFragment();
        secondFragment.setArguments(args);
        fragmentTransaction.replace(R.id.first_fragment, secondFragment).addToBackStack(null).commit();
    }

    @Override
    public void onItemLongClick(int position) // RecyclerViewInterface method
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("Are you sure you want to delete this location?");
        builder.setCancelable(true);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                // remove from recyclerview
                locationArrayList.remove(position);
                adapter.notifyItemRemoved(position);

                // delete from database
                try
                {
                    String sql = "DELETE FROM locations WHERE name = ?";
                    SQLiteStatement sqLiteStatement = database.compileStatement(sql);

                    View view = binding.recyclerView.getChildAt(position); // This will give the entire row(child) from RecyclerView
                    TextView textView = view.findViewById(R.id.recyclerViewTextView);
                    String name = textView.getText().toString();

                    sqLiteStatement.bindString(1, name);
                    sqLiteStatement.execute();
                }

                catch (Exception exception)
                {
                    exception.printStackTrace();
                }

                Toast.makeText(getContext(), "Location deleted successfully", Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                // do nothing
                dialogInterface.cancel();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}