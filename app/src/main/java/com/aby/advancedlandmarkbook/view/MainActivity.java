package com.aby.advancedlandmarkbook.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.widget.Toolbar;

import com.aby.advancedlandmarkbook.R;
import com.aby.advancedlandmarkbook.databinding.ActivityMainBinding;

// view binding - menu - fragment - recycler view - location request - maps
// shared preferences - sqlite database - alertdialog - toast - searchview kullanilabilir
public class MainActivity extends AppCompatActivity
{
    private ActivityMainBinding binding;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        FirstFragment firstFragment = new FirstFragment();
        openFragment(firstFragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if (item.getItemId() == R.id.add_place)
        {
            SecondFragment secondFragment = new SecondFragment();
            openFragment(secondFragment);
        }

        return super.onOptionsItemSelected(item);
    }

    private void openFragment(final Fragment fragment)
    {
        fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}