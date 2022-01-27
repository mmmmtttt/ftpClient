package com.ss.ftpClient.tasks;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.ss.ftpClient.R;
import com.ss.ftpClient.Utils;
import com.ss.ftpClient.exceptions.DataLinkOpenException;
import com.ss.ftpClient.exceptions.FileTransmissionException;
import com.ss.ftpClient.gui.MyApplication;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 多线程下载文件
 */
public class ConcurrentUploadTask extends AsyncTask<List<String>, String, Boolean> {
    private static final String TAG = "ConcurrentUploadTask";
    Socket cmdSocket;
    Context context;
    AlertDialog dialog;
    BufferedReader reader;
    PrintWriter writer;

    public ConcurrentUploadTask(Context context) {
        cmdSocket = UserSession.userSession.cmdSocket;
        this.context = context;
    }

    /**
     * 在后台任务开始执行之前调用，用于进行一些界面上的初始化操作
     */
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = new AlertDialog.Builder(context)
                .setTitle("upload concurrently")
                .setMessage("task received")
                .setIcon(R.drawable.ic_baseline_continue_24)
                .create();
        dialog.show();
    }

    /**
     * 遍历所有选中的文件，创建连接发送文件
     */
    @Override
    protected Boolean doInBackground(List<String>... fileList) {
        Log.e(TAG, "doInBackground: ");
        //打开输入输出流
        reader = DataConnection.getInstance().getReader();
        writer = DataConnection.getInstance().getWriter();
        //传输每个选中的文件
        for (String filePath :
                fileList[0]) {
            try {
                uploadSingleFile(filePath);
            } catch (FileTransmissionException e) {
                Log.e(TAG, "doInBackground: transmission task fail");
                return false;
            }
        }
        Log.i(TAG, "doInBackground: transmission task success");
        return true;
    }

    /**
     * 双线程上传单个文件,stor命令
     */
    public void uploadSingleFile(String filePath) throws FileTransmissionException {
        File file = new File(filePath);
        publishProgress("uploading:" + filePath);
        Log.i(TAG, "uploadSingleFile: " + filePath);
        //切分文件
        long[] range = splitFile(file.length());
        String response;
        Socket[] dataSockets = new Socket[2];
        try {
            //建立数据连接
            dataSockets[0] = DataConnection.getInstance().sendPasvCmd();
            dataSockets[1] = DataConnection.getInstance().sendPasvCmd();

            //发送stor命令
            writer.println(String.format("STOR %s %d %d", file.getName(),range[0],range[1]));
            writer.println(String.format("STOR %s %d %d", file.getName(),range[2],range[3]));
            for (int i = 0; i < 2; i++) {
                response = reader.readLine();
                Utils.assertResponse(response, "125");
                Log.i(TAG, "uploadSingleFile: " + response);
            }

            //双线程传输文件
            new UploadFileThread(filePath,range[0],range[1],dataSockets[0]).start();
            new UploadFileThread(filePath,range[2],range[3],dataSockets[1]).start();

            //确认响应正确
            response = reader.readLine();
            Log.i(TAG, "uploadSingleFile: " + response);
            Utils.assertResponse(response, "250");
            publishProgress("success:" + filePath);
        } catch (IOException | DataLinkOpenException e) {
            publishProgress("error:" + filePath);
            throw new FileTransmissionException(filePath);
        } finally {
            try {
                dataSockets[0].close();
                dataSockets[1].close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public long[] splitFile(long fileLength){
        long[] range =  new long[4];
        range[0] = 0;
        range[1] = fileLength/2;
        range[2] = range[1];
        range[3] = fileLength;
        return range;
    }


    @Override
    protected void onProgressUpdate(String... values) {
        Log.e(TAG, "onProgressUpdate: " + values[0]);
        dialog.setMessage(values[0]);
    }

    /**
     * doInBG完成后在ui线程中对其返回值处理
     */
    @Override
    protected void onPostExecute(Boolean uploadSuccess) {
        dialog.dismiss();
        if (uploadSuccess) {
            Toast.makeText(MyApplication.getContext(), "upload successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MyApplication.getContext(), "upload error...", Toast.LENGTH_SHORT).show();
        }
    }
}
