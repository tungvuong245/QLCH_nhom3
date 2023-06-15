package com.example.qlch;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.qlch.databinding.ActivityMainBinding;
import com.example.qlch.home.HomeFragment;
import com.example.qlch.maket.MarketFragment;
import com.example.qlch.product.ProductFragment;
import com.example.qlch.setting.SettingFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Window window = getWindow();
        getSupportFragmentManager().beginTransaction().add(R.id.fade_control, HomeFragment.newInstance()).commit();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        window.setStatusBarColor(getColor(R.color.white));
        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home_fragment:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fade_control, HomeFragment.newInstance()).commit();
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                        window.setStatusBarColor(getColor(R.color.white));
                        break;
                    case R.id.maket_fragment:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fade_control, MarketFragment.newInstance()).commit();
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                        window.setStatusBarColor(getColor(R.color.white));
                        break;
                    case R.id.menu_fragment:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fade_control, ProductFragment.newInstance()).commit();
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                        window.setStatusBarColor(getColor(R.color.white));
                        break;
                    case R.id.setting_fragment:
                        getSupportFragmentManager().beginTransaction().replace(R.id.fade_control, SettingFragment.newInstance()).commit();
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                        window.setStatusBarColor(getColor(R.color.brown_120));
                        break;
                }
                return true;
            }
        });
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {

                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        Log.d("xxx",""+ token);

                    }
                });


    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }






}