package com.ss.ftpClient.tasks;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;
import static androidx.navigation.Navigation.findNavController;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.ss.ftpClient.R;
import com.ss.ftpClient.databinding.FragmentLoginBinding;
import com.ss.ftpClient.gui.MyApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * 异步执行登陆任务，传入UserSession参数
 */
public class LoginTask extends AsyncTask<Void, String, Boolean> {
    FragmentLoginBinding binding;

    /**
     * 从login fragment中传递binding，以修改控件
     */
    public void setBinding(FragmentLoginBinding binding) {
        this.binding = binding;
    }

    /**
     * 在子线程中登陆
     */
    @Override
    protected Boolean doInBackground(Void... voids) {
        Log.e(TAG, "doInBackground: ");
        //得到用户状态
        String ip = UserSession.getUserSession().serverip;
        int port = UserSession.getUserSession().serverPort;
        String username = UserSession.getUserSession().username;
        String password = UserSession.getUserSession().password;

        //进行socket连接
        Socket socket;
        try {
            socket = new Socket(ip, port);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            UserSession.getUserSession().setStream(reader,writer);

            String response = reader.readLine();
            Log.i(TAG, "doInBackground: "+response);
            //成功建立连接
            if (!response.startsWith("220")) {
                publishProgress("server error");//上传任务过程中的进度，调用Onprogressupdate
                return false;
            }
            //写USER指令，并读取处理响应
            writer.println(String.format("USER %s", username));
            response = reader.readLine();
            Log.i(TAG, "doInBackground: "+response);
            if (response.startsWith("230")) {//匿名用户
                return true;
            } else if (response.startsWith("332")) {//不存在此用户名的用户,关闭连接
                publishProgress("user not exist");
                return false;
            } else {//密码验证
                //PASS指令
                writer.println(String.format("PASS %s", password));
                response = reader.readLine();
                Log.i(TAG, "doInBackground: "+response);
                //登录成功
                if (response.startsWith("530")) {
                    publishProgress("illegal access");
                    return false;
                } else {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 在ui线程更新错误的提示信息
     */
    @Override
    protected void onProgressUpdate(String... errorMessage) {
        Toast.makeText(MyApplication.getContext(), errorMessage[0], Toast.LENGTH_SHORT).show();
    }

    /**
     * doInBG完成后在ui线程中对其返回值处理
     */
    @Override
    protected void onPostExecute(Boolean loginSuccess) {
        if (loginSuccess) {
            MyApplication.getNavController().navigate(R.id.action_loginFragment_to_homeFragment);
        } else {
            binding.inputPass.setText("");
            binding.inputUsername.setText("");
        }
    }
}
