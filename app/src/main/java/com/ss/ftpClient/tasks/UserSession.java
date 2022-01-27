package com.ss.ftpClient.tasks;

import android.util.Log;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class UserSession {
    private static final String TAG = "UserSession";
    public static UserSession userSession;
    String username;
    String password;
    String serverip;
    int serverPort;
    Socket cmdSocket;
    boolean isPasvMode = true;   //默认被动
    boolean isBinaryType = false; //默认ascii
    Socket dataSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    boolean doubleThread = false;//优化：双线程传输一个文件，默认关

    private UserSession() {
    }

    public static UserSession getUserSession() {
        if (userSession == null) {
            userSession = new UserSession();
        }
        return userSession;
    }

    public void initUserSession(String username, String password, String serverip, int port) {
        this.username = username;
        this.password = password;
        this.serverip = serverip;
        this.serverPort = port;
    }

    public String getUsername() {
        return username;
    }

    public void setPasvMode(boolean pasvMode) {
        isPasvMode = pasvMode;
        Log.d(TAG, "setPasvMode:"+isPasvMode);
    }

    public void setBinaryType(boolean binaryType) {
        isBinaryType = binaryType;
    }

    public void reset(){
        try {
            cmdSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        isPasvMode = true;
        isBinaryType = false;
        try {
            DataConnection.getInstance().getReader().close();
            DataConnection.getInstance().getWriter().close();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            DataConnection.reset();
        }
    }

    public void setStream(BufferedReader reader, PrintWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    public void setDoubleThread(boolean doubleThread) {
        this.doubleThread = doubleThread;
    }

    public boolean isDoubleThread() {
        return doubleThread;
    }

    public PrintWriter getWriter() {
        return writer;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public void setDataSocket(Socket dataSocket) {
        this.dataSocket = dataSocket;
    }

    public Socket getDataSocket() {
        return dataSocket;
    }
}
