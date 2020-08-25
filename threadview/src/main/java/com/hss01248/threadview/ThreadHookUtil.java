package com.hss01248.threadview;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;

import de.robv.android.xposed.DexposedBridge;
import de.robv.android.xposed.XC_MethodHook;


public class ThreadHookUtil {
    static String TAG = "ThreadMethodHook";
     volatile static int count = 0;
    static WeakHashMap<Thread,StackTraceElement[]> map = new WeakHashMap<>();

    static boolean initSuccess = false;

    public static List<WeakReference<Thread>> getAll(){
        if(!initSuccess){
            return new ArrayList<>();
        }
        List<WeakReference<Thread>> list = new ArrayList<>();
      Iterator<Thread> threadIterator =  map.keySet().iterator();
      while (threadIterator.hasNext()){
          list.add(new WeakReference<>(threadIterator.next()));
      }
      Log.d(TAG,"getall,size:"+list.size());
      return list;

    }

    public static void hookThread(Application application){
        if(isEmulator(application)){
            Log.w(TAG,"模拟器,不hook");
            return;
        }
        if(!isDebuggable(application)){
            Log.w(TAG,"非debug包,不hook");
        }
        DexposedBridge.hookAllConstructors(Thread.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Thread thread = (Thread) param.thisObject;
                //thread.setUncaughtExceptionHandler(new SafeHandler());
                Class<?> clazz = thread.getClass();
                if (clazz != Thread.class) {
                    Log.d(TAG, "found class extend Thread:" + clazz);
                    //DexposedBridge.findAndHookMethod(clazz, "run", new ThreadMethodHook());
                }
                DexposedBridge.findAndHookMethod(clazz, "start", new ThreadStartMethodHook());
                Log.d(TAG, "Thread: " + thread.getName() + " class:" + thread.getClass() +  " is created.");
            }
        });
        //DexposedBridge.findAndHookMethod(Thread.class, "run", new ThreadMethodHook());
        DexposedBridge.findAndHookMethod(Thread.class, "start", new ThreadStartMethodHook());
        initSuccess = true;
    }

     static boolean isDebuggable(Application application) {
        boolean debuggable = false;
        PackageManager pm = application.getPackageManager();
        try{
            ApplicationInfo appinfo = pm.getApplicationInfo(application.getPackageName(), 0);
            debuggable = (0 != (appinfo.flags & ApplicationInfo.FLAG_DEBUGGABLE));
        }catch(PackageManager.NameNotFoundException e){
            /*debuggable variable will remain false*/
        }
        return debuggable;
    }

     static boolean isEmulator(Application application) {
        boolean checkProperty = Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.toLowerCase().contains("vbox")
                || Build.FINGERPRINT.toLowerCase().contains("test-keys")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
        if (checkProperty) return true;

        String operatorName = "";
        TelephonyManager tm = (TelephonyManager) application.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            String name = tm.getNetworkOperatorName();
            if (name != null) {
                operatorName = name;
            }
        }
        boolean checkOperatorName = operatorName.toLowerCase().equals("android");
        if (checkOperatorName) return true;

        String url = "tel:" + "123456";
        Intent intent = new Intent();
        intent.setData(Uri.parse(url));
        intent.setAction(Intent.ACTION_DIAL);
        boolean checkDial = intent.resolveActivity(application.getPackageManager()) == null;
        if (checkDial) return true;

//        boolean checkDebuggerConnected = Debug.isDebuggerConnected();
//        if (checkDebuggerConnected) return true;

        return false;
    }

   static class ThreadMethodHook extends XC_MethodHook {

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            super.beforeHookedMethod(param);
            Thread t = (Thread) param.thisObject;
            Log.i(TAG, "thread:" + t + ", started..");
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            super.afterHookedMethod(param);
            Thread t = (Thread) param.thisObject;
            Log.i(TAG, "thread:" + t + ", exit..");
        }
    }

    static class ThreadStartMethodHook extends XC_MethodHook {

        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            super.beforeHookedMethod(param);
            Thread t = (Thread) param.thisObject;
            Log.i(TAG, "thread:" + t + ", start begin..");
        }

        @Override
        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            super.afterHookedMethod(param);
            Thread t = (Thread) param.thisObject;
            Log.i(TAG, "thread:" + t + ", started");
            count++;
            Log.i(TAG, "thread count:" + count);

           StackTraceElement[] stackTraceElements =  new Exception().getStackTrace();
            StackTraceElement[] stackTraceElements2 = new StackTraceElement[stackTraceElements.length -5];
            for (int i = 0; i < stackTraceElements.length-5; i++) {
                stackTraceElements2[i] = stackTraceElements[i+5];
            }
            map.put(t,stackTraceElements2);
            Log.i(TAG, "map count2:" + map.keySet().size());

            Exception exception = new Exception();
            exception.setStackTrace(stackTraceElements2);
            exception.printStackTrace();
            /*new Exception().printStackTrace();

            Exception exception = new Exception();
            exception.setStackTrace(t.getStackTrace());
            exception.printStackTrace();*/
        }
    }




    static class SafeHandler implements Thread.UncaughtExceptionHandler{

        @Override
        public void uncaughtException(Thread t, Throwable e) {
           Log.e(TAG,"thread crash:"+t.getName());
            e.printStackTrace();
        }
    }

    public static void showThreadList(Activity activity){
        if(!initSuccess){
            return ;
        }
        Dialog dialog = new Dialog(activity);
        ThreadListViewHolder holder = new ThreadListViewHolder(activity);
        dialog.setContentView(holder.root);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getWindow().getAttributes().width = activity.getResources().getDisplayMetrics().widthPixels;
            }
        });
       dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
       // dialog.setContentView();
    }
}
