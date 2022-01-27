package com.ss.ftpClient.tasks;

import android.os.AsyncTask;
import android.util.Log;

import androidx.navigation.NavController;

import com.ss.ftpClient.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class QuitTask extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "QuitTask";
    NavController navController;
    BufferedReader reader;
    PrintWriter writer;

    public QuitTask(NavController navController) {
        this.navController = navController;
        reader = DataConnection.getInstance().getReader();
        writer = DataConnection.getInstance().getWriter();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            writer.println("QUIT");
            String response = reader.readLine();
            Log.i(TAG, "doInBackground: "+response);
            //成功退出
            if (response.startsWith("221")) {
                Log.d(TAG, "doInBackground: quit");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            UserSession.getUserSession().reset();//重置session信息，关闭socket
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
        navController.navigate(R.id.action_homeFragment_to_loginFragment);
    }
}
