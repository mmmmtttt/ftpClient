package com.ss.ftpClient.gui;

import android.app.Application;
import android.content.Context;

import androidx.navigation.NavController;

public class MyApplication extends Application {
    public static final int CODE_FOR_READ_PERMISSION = 517;
    private static Context mContext;
    private static NavController navController;

    public static Context getContext(){
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static void setNavController(NavController navControl) {
        navController = navControl;
    }

    public static NavController getNavController() {
        return navController;
    }
}
