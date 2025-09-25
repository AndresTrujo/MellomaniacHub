package com.trujo.mellomaniachub;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.trujo.mellomaniachub.fragments.ListsFragment; // Import ListsFragment
import com.trujo.mellomaniachub.fragments.SearchFragment;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Cargar el fragmento de búsqueda por defecto
        if (savedInstanceState == null) {
            loadFragment(new SearchFragment(), "SearchFragment"); // Added a tag
        }

        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                String tag = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_search) {
                    selectedFragment = new SearchFragment();
                    tag = "SearchFragment";
                } else if (itemId == R.id.nav_lists) { // Handle new Lists tab
                    selectedFragment = new ListsFragment();
                    tag = "ListsFragment";
                }
                // else if (itemId == R.id.nav_favorites) { // Ejemplo para futuras pestañas
                //    selectedFragment = new FavoritesFragment();
                //    tag = "FavoritesFragment";
                // } else if (itemId == R.id.nav_profile) {
                //    selectedFragment = new ProfileFragment();
                //    tag = "ProfileFragment";
                // }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment, tag);
                    return true;
                }
                return false;
            }
        });
    }

    private void loadFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
        // Opcional: addToBackStack(null) si quieres que el botón atrás navegue entre fragmentos
        // Si decides usar addToBackStack, considera pasar el 'tag' como nombre para la transacción.
        fragmentTransaction.commit();
    }
}
