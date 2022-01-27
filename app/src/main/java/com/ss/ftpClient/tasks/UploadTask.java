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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 异步传输选中文件
 */
public class UploadTask extends AsyncTask<List<String>, String, Boolean> {
    private static final String TAG = "UploadTask";
    Socket cmdSocket;
    Context context;
    AlertDialog dialog;
    BufferedReader reader;
    PrintWriter writer;
    Socket dataSocket;

    public UploadTask(Context context) {
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
                .setTitle("upload progress")
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
     * 上传单个文件,stor命令
     */
    public void uploadSingleFile(String filePath) throws FileTransmissionException {
        File file = new File(filePath);
        publishProgress("uploading:" + filePath);
        Log.i(TAG, "uploadSingleFile: " + filePath);
        String response;
        try {
            dataSocket = DataConnection.getInstance().setUpConnection("STOR", file.getName());
            response = reader.readLine();
            Utils.assertResponse(response, "125");
            Log.i(TAG, "uploadSingleFile: " + response);

            writeFileData(dataSocket, filePath);
            response = reader.readLine();
            Log.i(TAG, "uploadSingleFile: " + response);
            Utils.assertResponse(response, "250");

            publishProgress("success:" + filePath);
        } catch (IOException | DataLinkOpenException e) {
            publishProgress("error:" + filePath);
            throw new FileTransmissionException(filePath);
        } finally {
            try {
                dataSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 向打开的dataSocket写文件数据
     */
    private void writeFileData(Socket dataSocket, String filePath) throws IOException {
        boolean isBinary = UserSession.getUserSession().isBinaryType;
        int count = 0;
        if (isBinary) {
//            try (
//                 BufferedOutputStream bo = new BufferedOutputStream(dataSocket.getOutputStream());
//                 BufferedInputStream bi = new BufferedInputStream(new FileInputStream(filePath))) {
//                byte[] buffer = new byte[1024];
//                int bytesRead = 0;
//                while ((bytesRead = bi.read(buffer)) != -1) {
//                    count+=bytesRead;
//                    bo.write(buffer);
//                    bo.flush();
//                    publishProgress("complete "+count+ " bytes...");
//                }
//            } catch (IOException e) {
//                Log.e(TAG, "sendFile: ioexception");
//                throw e;
//            }
            OutputStream out = dataSocket.getOutputStream();
            try (FileInputStream in = new FileInputStream(filePath)) {
                int bytesRead = 0;
                byte[] buffer = new byte[1024];
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                    out.flush();
                    count += bytesRead;
                    publishProgress("complete " + count + " bytes...");
                }
                out.close();
            } catch (IOException e) {
                Log.e(TAG, "sendFile: ioexception");
                throw e;
            }
        } else {//ASCII模式
            try (
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(dataSocket.getOutputStream(), StandardCharsets.US_ASCII));
                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.US_ASCII))) {
                String line;
                while ((line = br.readLine()) != null) {
                    bw.write(line + "\r\n");
                    count++;
                    publishProgress("complete " + count + " lines...");
                }
            } catch (IOException e) {
                Log.e(TAG, "sendFile: ioexception");
                throw e;
            }
        }
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
