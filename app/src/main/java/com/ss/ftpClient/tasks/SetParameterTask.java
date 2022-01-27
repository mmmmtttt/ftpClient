package com.ss.ftpClient.tasks;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.ss.ftpClient.Utils;
import com.ss.ftpClient.gui.MyApplication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 异步任务处理设置传输参数 struc/mode/type
 */
public class SetParameterTask extends AsyncTask<String,Void,Boolean> {
    private static final String TAG = "SetParameterTask";
    UserSession userSession;
    PrintWriter writer;
    BufferedReader reader;

    public SetParameterTask(){
        userSession = UserSession.getUserSession();
        writer = userSession.getWriter();
        reader = userSession.getReader();
    }

    @Override
    protected Boolean doInBackground(String... cmds) {
        String response;
        writer.println(cmds[0]);
        try {
            response = reader.readLine();
            Log.d(TAG, "doInBackground: "+response);
            Utils.assertResponse(response, "200");
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: "+e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success){
            Toast.makeText(MyApplication.getContext(), "set successfully!", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(MyApplication.getContext(), "set error...", Toast.LENGTH_SHORT).show();
        }
    }
}
