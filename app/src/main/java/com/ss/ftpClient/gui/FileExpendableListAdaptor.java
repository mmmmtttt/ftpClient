package com.ss.ftpClient.gui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.ss.ftpClient.R;

import java.util.Arrays;
import java.util.List;

class FileExpendableListAdaptor extends BaseExpandableListAdapter {
    String[] group;
    String[][] child;
    Context context;
    List<String> chosen;

    FileExpendableListAdaptor(String[] group, String[][] child, Context context,List<String> chosen) {
        this.group = group;
        this.child = child;
        this.context = context;
        this.chosen = chosen;
    }

    @Override
    public int getGroupCount() {
        return group.length;
    }

    @Override
    public int getChildrenCount(int i) {
        return child[i].length;
    }

    @Override
    public Object getGroup(int i) {
        return child[i];
    }

    @Override
    public Object getChild(int i, int i1) {
        return child[i][i1];
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_elv_group, null);
        }
        TextView text = (TextView) view.findViewById(R.id.tv_groupName);
        text.setText(group[i]);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.cb_elv_group);//全选按钮
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked){
                    chosen.addAll(Arrays.asList(child[i]));
                }else {
                    chosen.removeAll(Arrays.asList(child[i]));
                }
                notifyDataSetChanged();//实现全选
            }
        });
        return view;
    }

    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_elv_child, null);
        }
        TextView text = (TextView) view.findViewById(R.id.tv_elv_childName);
        text.setText(child[i][i1]);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.cb_elv_child);
        if (chosen.contains(child[i][i1])){//全选
            checkBox.setChecked(true);
        }else {
            checkBox.setChecked(false);
        }
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked){
                    chosen.add(child[i][i1]);
                }else {
                    chosen.remove(child[i][i1]);
                }
            }
        });
        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return true;
    }


}
