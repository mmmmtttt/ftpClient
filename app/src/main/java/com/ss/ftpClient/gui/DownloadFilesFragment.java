package com.ss.ftpClient.gui;

import static androidx.navigation.Navigation.findNavController;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.ss.ftpClient.R;
import com.ss.ftpClient.databinding.FragmentDownloadFilesBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * 用expendableListView来展示服务端可下载的文件列表
 */
public class DownloadFilesFragment extends Fragment {
    FragmentDownloadFilesBinding binding;
    List<String> chosenFile;
    NavController navController;
    String[] catagory= null;
    String[][] file = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDownloadFilesBinding.inflate(inflater, container, false);
        navController = findNavController(getActivity().findViewById(R.id.fragment_main));
        initData();
        chosenFile = new ArrayList<>();
        FileExpendableListAdaptor adaptor = new FileExpendableListAdaptor(catagory, file, getActivity(),chosenFile);
        binding.fileListView.setAdapter(adaptor);

        binding.btConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] result = new String[chosenFile.size()];
                chosenFile.toArray(result);
                Bundle bundle = new Bundle();
                bundle.putStringArray("chosenFile",result);
                navController.navigate(R.id.action_downloadFilesFragment_to_homeFragment,bundle);
            }
        });
        return binding.getRoot();
    }

    public void initData(){
        catagory = new String[]{"small", "big"};
        file = new String[][]{
                {"small9980", "small9981", "small9982", "small9983", "small9984", "small9985",
                        "small9986", "small9987", "small9988", "small9989", "small9990", "small9991",
                        "small9992", "small9993", "small9994", "small9995", "small9996", "small9997", "small9998", "small9999"},
                {"big0000", "big0001"}};
    }
}

