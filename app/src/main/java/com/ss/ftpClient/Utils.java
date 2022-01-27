package com.ss.ftpClient;

import android.util.Log;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class Utils {
    static InetAddress ip = null;
    public static boolean isNum(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i)))
                return false;
        }
        return true;
    }

    /**
     * 获取本机内网ip（要求客户端和服务器在同一局域网内）
     */
    public static InetAddress getLocalIpAddress(){
        if (ip!=null){
            return ip;
        }
        try {
            Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = allNetInterfaces.nextElement();
                if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp()) {
                    continue;
                } else {
                    Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        ip = addresses.nextElement();
                        if (ip != null && ip instanceof Inet4Address) {
                            return ip;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("Utils", "getIpAddress:IP地址获取失败");
        }
        return null;
    }

    public static void assertResponse(String res, String expectedCode) throws IOException {
        if (!res.startsWith(expectedCode)) {
            throw new IOException("response not " + expectedCode);
        }
    }
}
