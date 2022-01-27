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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class DownloadTask extends AsyncTask<String[], String, Boolean> {
    private static final String TAG = "DownloadTask";
    Socket cmdSocket;
    Context context;
    AlertDialog dialog;
    BufferedReader reader;
    PrintWriter writer;
    Socket dataSocket;

    public DownloadTask(Context context) {
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
                .setTitle("download progress")
                .setMessage("task received")
                .setIcon(R.drawable.ic_baseline_continue_24)
                .create();
        dialog.show();
    }

    /**
     * 遍历所有选中的文件，创建连接发送文件
     */
    @Override
    protected Boolean doInBackground(String[]... files) {
        Log.e(TAG, "doInBackground: ");
        //打开输入输出流
        reader = DataConnection.getInstance().getReader();
        writer = DataConnection.getInstance().getWriter();
        for (String filename :
                files[0]) {
            try {
                //传输选中的文件
                downloadSingleFile(filename);
            } catch (FileTransmissionException e) {
                Log.e(TAG, "doInBackground: transmission task fail");
                return false;
            }
        }
        Log.i(TAG, "doInBackground: transmission task success");
        return true;
    }

    /**
     * 下载单个文件,retr命令
     */
    public void downloadSingleFile(String fileName) throws FileTransmissionException {
        publishProgress("downloading:" + fileName);
        Log.i(TAG, "downloadFile: " + fileName);
        String response;
        try {
            dataSocket = DataConnection.getInstance().setUpConnection("RETR", fileName);
            response = reader.readLine();
            Utils.assertResponse(response, "125");
            Log.i(TAG, "downloadFile: " + response);

            saveFileData(dataSocket, fileName);
            response = reader.readLine();
            Log.i(TAG, "downloadFile: " + response);
            Utils.assertResponse(response, "250");

            publishProgress("success:" + fileName);
        } catch (IOException | DataLinkOpenException e) {
            publishProgress("error:" + fileName);
            throw new FileTransmissionException(fileName);
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
    private void saveFileData(Socket dataSocket, String fileName) throws IOException {
        //文件已经存在，直接覆盖
        File file = new File(context.getExternalFilesDir(null)+File.separator+fileName);
        if (file.exists()){
            file.delete();
            file.createNewFile();
        }

        boolean isBinary = UserSession.getUserSession().isBinaryType;
        int count = 0;
        if (isBinary) {
//            try (
//                    BufferedInputStream bi = new BufferedInputStream(dataSocket.getInputStream());
//                    BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(file))) {
//                byte[] buffer = new byte[1024];
//                int bytesRead = 0;
//                while ((bytesRead = bi.read(buffer)) != -1) {
//                    bo.write(buffer);
//                    count+=bytesRead;
//                    publishProgress("complete "+count+" bytes...");
//                }
//            } catch (IOException e) {
//                Log.e(TAG, "saveFile: ioexception");
//                throw e;
//            }

            InputStream in = dataSocket.getInputStream();
            try (FileOutputStream out = new FileOutputStream(file)) {
                int bytesRead = 0;
                byte[] buffer = new byte[1024];
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer,0,bytesRead);
                    out.flush();
                    count += bytesRead;
                    publishProgress("complete " + count + " bytes...");
                }
            }catch (IOException e) {
                Log.e(TAG, "receiveFile: ioexception");
                throw e;
            } finally {
                in.close();
            }
        } else {//ASCII模式
            try (
                    BufferedReader br = new BufferedReader(new InputStreamReader(dataSocket.getInputStream(), StandardCharsets.US_ASCII));
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),StandardCharsets.US_ASCII))) {
                String line;
                while ((line = br.readLine()) != null) {
                    count++;
                    publishProgress("complete "+count+" lines...");
                    bw.write(line + "\r\n");
                }
            } catch (IOException e) {
                Log.e(TAG, "saveFile: ioexception");
                throw e;
            }
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
        dialog.setMessage(values[0]);
    }

    /**
     * doInBG完成后在ui线程中对其返回值处理
     */
    @Override
    protected void onPostExecute(Boolean downloadSuccess) {
        dialog.dismiss();
        if (downloadSuccess) {
            Toast.makeText(MyApplication.getContext(), "download successfully!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MyApplication.getContext(), "download error...", Toast.LENGTH_SHORT).show();
        }
    }
}
