package com.hss01248.threadview;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;

public class ThreadListActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(!ThreadHookUtil.initSuccess){
            finish();
            return ;
        }
        ThreadListViewHolder holder = new ThreadListViewHolder(this);
       setContentView(holder.root);
    }
}
