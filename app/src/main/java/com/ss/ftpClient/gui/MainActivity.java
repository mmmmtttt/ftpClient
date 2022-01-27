package com.ss.ftpClient.gui;

import static androidx.navigation.Navigation.findNavController;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;

import com.ss.ftpClient.R;

public class MainActivity extends AppCompatActivity {

    NavController navController = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navController = findNavController(findViewById(R.id.fragment_main));
        //将App bar与NavController绑定，当NavController完成Fragment切换时，系统会自动在App bar中完成一些常见操作
        AppBarConfiguration appBarConf = new AppBarConfiguration.Builder(R.id.loginFragment,R.id.homeFragment).build();
        NavigationUI.setupActionBarWithNavController(this,navController,appBarConf);
        setTitle("Client");
    }

    //使用toolbar的后退按钮后退
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = findNavController(findViewById(R.id.fragment_main));
        return navController.navigateUp()||super.onSupportNavigateUp();
    }
}