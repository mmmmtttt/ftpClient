package com.ss.ftpClient.tasks;

import android.util.Log;

import com.ss.ftpClient.Utils;
import com.ss.ftpClient.exceptions.DataLinkOpenException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 单例,封装了在真正传输文件之间客户端和服务端关于传输参数的协商
 */
public class DataConnection {
    private static DataConnection dataConnection;
    private static final String TAG = "DataConnection";

    private Socket dataSocket;

    private BufferedReader reader;
    private PrintWriter writer;
    private UserSession userSession;

    private DataConnection() {
        userSession = UserSession.getUserSession();
        reader = userSession.getReader();
        writer = userSession.getWriter();
    }

    public static DataConnection getInstance() {
        if (dataConnection == null) {
            dataConnection = new DataConnection();
        }
        return dataConnection;
    }

    /**
     * 发送传输时指令，包括pass/port和retr/stor
     */
    public Socket setUpConnection(String cmdCode, String fileName) throws DataLinkOpenException {
        boolean isPasvMode = userSession.isPasvMode;
        //发送pass或者pasv
        if (isPasvMode) {
            dataSocket = sendPasvCmd();
        } else {
            sendPortCmd();
        }
        //发送retr/stor
        writer.println(String.format("%s %s", cmdCode, fileName));
        return dataSocket;
    }

    /**
     * 发送pasv 命令给服务端,打开数据连接
     */
    public Socket sendPasvCmd() throws DataLinkOpenException {
        String response;
        Socket dataSocket = null;
        try {
            writer.println("pasv");
            response = reader.readLine();
            Log.i(TAG, "sendPasvCmd: " + response);
            Utils.assertResponse(response, "227");
            response = response.replace("227 Entering Passive Mode (", "").replace(").", "");
            String[] info = response.split(",");
            //从响应中提取服务端的数据端口和ip
            String passHost = "";
            for (int i = 0; i < 4; i++) {
                passHost += info[i] + ".";
            }
            passHost = passHost.substring(0, passHost.length() - 1);
            int passPort = Integer.parseInt(info[5]) + (Integer.parseInt(info[4]) << 8);
            dataSocket = new Socket(passHost, passPort);
            response = reader.readLine();
            Log.i(TAG, "sendPasvCmd: " + response);
            Utils.assertResponse(response, "220");
        } catch (IOException e) {
            Log.e(TAG, "sendPasvCmd: " + e.getMessage());
            throw new DataLinkOpenException(e.getMessage());
        }
        return dataSocket;
    }

    /**
     * 发送port命令给服务端，打开数据连接
     */
    private void sendPortCmd() throws DataLinkOpenException {
        String localAddress = Utils.getLocalIpAddress().getHostAddress();
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(0);
            int port = serverSocket.getLocalPort();
            int p1 = port >> 8;
            int p2 = port - (p1 << 8);
            String request = String.format("PORT %s,%d,%d", localAddress.replace(".", ","), p1, p2);
            writer.println(request);
            dataSocket = serverSocket.accept();
            String response = reader.readLine();
            Log.i(TAG, "sendPortCmd: " + response);
            Utils.assertResponse(response, "220");
        } catch (IOException e) {
            Log.e(TAG, "sendPortCmd: " + e.getMessage());
            throw new DataLinkOpenException(e.getMessage());
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public BufferedReader getReader() {
        return reader;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public static void reset(){
        dataConnection=null;
    }
}
