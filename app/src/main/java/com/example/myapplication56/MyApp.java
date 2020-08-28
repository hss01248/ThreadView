package com.example.myapplication56;

import android.app.Application;

import com.hss01248.threadview.ThreadHookUtil;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //ThreadHookUtil.hookThread(this);
    }
}
