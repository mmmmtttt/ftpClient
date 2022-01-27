package com.ss.ftpClient.gui;

import static androidx.navigation.Navigation.findNavController;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ss.ftpClient.R;
import com.ss.ftpClient.Utils;
import com.ss.ftpClient.databinding.FragmentLoginBinding;
import com.ss.ftpClient.tasks.LoginTask;
import com.ss.ftpClient.tasks.UserSession;

public class LoginFragment extends Fragment {
    FragmentLoginBinding binding;
    NavController navController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        navController = findNavController(getActivity().findViewById(R.id.fragment_main));
        MyApplication.setNavController(navController);
        binding.btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPara()){//检查参数通过
                    LoginTask loginTask = new LoginTask();
                    loginTask.setBinding(binding);
                    loginTask.execute();
                }
            }
        });

        //TODO:测试时的捷径，最后删掉
        binding.btLoginTest.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                UserSession.getUserSession().initUserSession("test","test","127.0.0.1",2121);
                LoginTask loginTask = new LoginTask();
                loginTask.setBinding(binding);
                loginTask.execute();
            }
        });

        return binding.getRoot();
    }

    private boolean checkPara(){
        String ip = binding.inputServerip.getText().toString();
        String port = binding.inputServerPort.getText().toString();
        String username = binding.inputUsername.getText().toString();
        String password = binding.inputPass.getText().toString();
        //ip和端口不应为空
        if (ip.length() == 0 || port.length() == 0) {
            Toast.makeText(getActivity(), "ip / port should not be blank", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!Utils.isNum(port)) {
            Toast.makeText(getActivity(), "port should be numbers", Toast.LENGTH_LONG).show();
            return false;
        }
        int port_num = Integer.parseInt(port);
        if (!(port_num >= 2048 && port_num <= 65536)) {
            Toast.makeText(getActivity(), "port_num should between 2048 and 65536", Toast.LENGTH_LONG).show();
            return false;
        }
        UserSession.getUserSession().initUserSession(username,password,ip,Integer.parseInt(port));
        return true;
    }
}