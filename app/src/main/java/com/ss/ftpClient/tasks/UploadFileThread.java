package com.ss.ftpClient.tasks;

import android.util.Log;

import com.ss.ftpClient.R;
import com.ss.ftpClient.Utils;
import com.ss.ftpClient.exceptions.DataLinkOpenException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * 多线程上传文件的工作线程
 */
public class UploadFileThread extends Thread {
    private static final String TAG = "UploadFileThread";
    long start;
    long end;
    private Socket dataSocket;
    String filePath;

    /**
     * 读[start,end)的字节
     */
    public UploadFileThread(String filePath, long start, long end, Socket dataSocket) {
        this.start = start;
        this.end = end;
        this.dataSocket = dataSocket;
        this.filePath = filePath;
    }

    /**
     * 优化策略1：randomaccessfile读写
     */
//    @Override
//    public void run() {
//        try (RandomAccessFile randomAccessFile = new RandomAccessFile(filePath, "r");
//             OutputStream out = dataSocket.getOutputStream()) {
//            randomAccessFile.seek(start);
//            int byteRead = 0;
//            long next = (int) start;
//            byte[] buffer = new byte[1024];
//            while ((byteRead = randomAccessFile.read(buffer)) != -1) {
//                if (next + byteRead > end) {//最后一次读
//                    out.write(buffer, 0, (int) (end - next));
//                    break;
//                }
//                out.write(buffer, 0, byteRead);
//                next+=byteRead;
//            }
//        } catch (IOException e) {
//            Log.e(TAG, "run: " + e.getMessage());
//        } finally {
//            try {
//                dataSocket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }

    /**
     * 优化策略2：java Nio直接内存映射
     */
    @Override
    public void run() {
        try(OutputStream out = dataSocket.getOutputStream()){
            //从start开始，把end-start大小的文件直接那映射到内存
            int len = (int)(end-start);
            MappedByteBuffer mappedByteBuffer = new RandomAccessFile(filePath, "rw")
                    .getChannel()
                    .map(FileChannel.MapMode.READ_WRITE, start, len);
            byte[] buffer = new byte[1024];
            try {
                while (mappedByteBuffer.hasRemaining()) {
                    mappedByteBuffer.get(buffer, 0, 1024);
                    out.write(buffer, 0, 1024);
                    out.flush();
                }
            }catch (BufferUnderflowException e){// If there are fewer than length bytes remaining in this buffer
                int remain = mappedByteBuffer.remaining();
                mappedByteBuffer.get(buffer,0, remain);//读出剩余的
                out.write(buffer, 0, remain);
                out.flush();
            }
            //单个byte读
//            for (int i = 0; i < len; i++) {
//                byte b = mappedByteBuffer.get();//Reads the byte at this buffer's current position, and then increments the position.
//                out.write(b);
//                out.flush();
//            }
        } catch (IOException e) {
            Log.e(TAG, "run: " + e.getMessage());
        } finally {
            try {
                dataSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
