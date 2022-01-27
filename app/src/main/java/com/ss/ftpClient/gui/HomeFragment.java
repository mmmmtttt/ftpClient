package com.ss.ftpClient.gui;

import static android.app.Activity.RESULT_OK;

import static androidx.navigation.Navigation.findNavController;

import static com.ss.ftpClient.gui.MyApplication.CODE_FOR_READ_PERMISSION;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Toast;

import com.ss.ftpClient.R;
import com.ss.ftpClient.Utils;
import com.ss.ftpClient.databinding.FragmentHomeBinding;
import com.ss.ftpClient.enums.Mode;
import com.ss.ftpClient.enums.Structure;
import com.ss.ftpClient.tasks.ConcurrentUploadTask;
import com.ss.ftpClient.tasks.DownloadTask;
import com.ss.ftpClient.tasks.QuitTask;
import com.ss.ftpClient.tasks.SetParameterTask;
import com.ss.ftpClient.tasks.UploadTask;
import com.ss.ftpClient.tasks.UserSession;
import com.leon.lfilepickerlibrary.LFilePicker;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HomeFragment extends Fragment {
    FragmentHomeBinding binding;
    NavController navController = null;
    UserSession userSession;
    final int SELECT_FILE = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navController = findNavController(getActivity().findViewById(R.id.fragment_main));
        userSession = UserSession.getUserSession();
    }

    @Override
    public void onResume() {
        super.onResume();
        //从下载文件选择页面返回
        if (getArguments() != null) {
            String[] chosenFile = getArguments().getStringArray("chosenFile");
            if (chosenFile.length!=0) {
                new DownloadTask(getActivity()).execute(chosenFile);
            }
        }
        setArguments(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        binding.labelWelcome.setText("Hi," + UserSession.getUserSession().getUsername());

        binding.spinnerMode.setSelection(0, true);
        binding.spinnerMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                boolean isPasv = "Passive Mode".equals(binding.spinnerMode.getSelectedItem().toString());
                userSession.setPasvMode(isPasv);
                Toast.makeText(getActivity(), "set successfully!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //设置初始时不触发，之后改变触发
        binding.spinnerType.setSelection(0, true);
        binding.spinnerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String type = binding.spinnerType.getSelectedItem().toString();
                String cmd = "TYPE ";
                if ("Binary".equals(type)) {
                    cmd += "B";
                    UserSession.getUserSession().setBinaryType(true);
                } else {
                    cmd += "A";
                    UserSession.getUserSession().setBinaryType(false);
                }
                new SetParameterTask().execute(cmd);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.spinnerStruc.setSelection(0, true);
        binding.spinnerStruc.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String cmd = "STRU "+Structure.getStru(i).getAbbr();
                new SetParameterTask().execute(cmd);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.spinnerTrans.setSelection(0, true);
        binding.spinnerTrans.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String cmd = "MODE "+Mode.getMode(i).getAbbr();
                new SetParameterTask().execute(cmd);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        //双线程优化
        binding.spinnerTrans.setSelection(0, true);
        binding.spinnerOptimize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String optimize = binding.spinnerOptimize.getSelectedItem().toString();
                if ("Single Thread".equals(optimize)){
                    UserSession.getUserSession().setDoubleThread(false);
                }else {
                    UserSession.getUserSession().setDoubleThread(true);
                }
                Toast.makeText(getActivity(), "set successfully!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.btUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileSelector();
            }
        });

        binding.btDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.action_homeFragment_to_downloadFilesFragment);
            }
        });

        binding.btLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Log Out")
                        .setMessage("Are you sure you want to log out?")
                        .setNegativeButton("no", (dialog, which) -> dialog.dismiss())
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new QuitTask(navController).execute();
                            }
                        });
                builder.show();
            }
        });

        //动态申请权限，使用兼容库就无需判断系统版本
        int hasReadStoragePermission = ContextCompat.checkSelfPermission(MyApplication.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
        if (hasReadStoragePermission != PackageManager.PERMISSION_GRANTED) {//无权限，执行操作
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, CODE_FOR_READ_PERMISSION);
        }

        return binding.getRoot();
    }

    public void openFileSelector() {
        new LFilePicker()
                .withSupportFragment(this)
                .withRequestCode(SELECT_FILE)
                .withStartPath("/storage/emulated/0/Download")
                .withBackgroundColor("#74B5AD")
                .start();
    }

    /**
     * 上传文件选择器选中的文件
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                //If it is a file selection mode, you need to get the path collection of all the files selected
                List<String> list = data.getStringArrayListExtra("paths");
                if (userSession.isDoubleThread()){
                    new ConcurrentUploadTask(getActivity()).execute(list);
                }else {
                    UploadTask uploadTask = new UploadTask(getActivity());
                    uploadTask.execute(list);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //通过requestCode来识别是否同一个请求
        if (requestCode == CODE_FOR_READ_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户同意，执行操作
                openFileSelector();
            }
        }
    }
}