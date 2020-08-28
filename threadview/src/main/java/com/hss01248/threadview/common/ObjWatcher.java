package com.hss01248.threadview.common;

import android.app.Application;
import android.util.Log;

import com.hss01248.threadview.ThreadHookUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.WeakHashMap;



public class ObjWatcher {
    static String TAG = "ObjWatcherHook";
    static TreeMap<Class,WeakHashMap<Object,BackInfo>> map = new TreeMap<>();


    public static void addObjWatch(Class clazz, Application application){
        if(ThreadHookUtil.isEmulator(application)){
            Log.w(TAG,"模拟器,不hook");
            return;
        }
        if(!ThreadHookUtil.isDebuggable(application)){
            Log.w(TAG,"非debug包,不hook");
        }
        if(!map.containsKey(clazz)){
            map.put(clazz,new WeakHashMap<>());
        }
       /* DexposedBridge.hookAllConstructors(clazz, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                if(!map.get(clazz).containsKey(param.thisObject)){
                    BackInfo info = new BackInfo();
                    info.obj = new WeakReference(param.thisObject);
                    info.createStack = Thread.currentThread().getStackTrace();
                    info.activity = "";
                    info.clazz = clazz;
                    map.get(clazz).put(param.thisObject,info);
                }
            }
        });*/
    }

    /**
     * 给单个tab
     * @param clazz
     * @return
     */
    public static List<BackInfo> getList(Class clazz){
        WeakHashMap<Object,BackInfo> weakHashMap = map.get(clazz);
        List<BackInfo> list = new ArrayList<>();
        Iterator threadIterator =  weakHashMap.keySet().iterator();
        while (threadIterator.hasNext()){
            BackInfo thread = weakHashMap.get(threadIterator.next());
            list.add(thread);
        }
        return list;
    }

    /**
     * 给整个viewpager
     * @return
     */
    public static List<List<BackInfo>> getAllList(){
        List<List<BackInfo>> list = new ArrayList<>();
        Iterator<Class> iterator = map.keySet().iterator();
        while (iterator.hasNext()){
            List<BackInfo> infos = getList(iterator.next());
            list.add(infos);
        }
        return list;
    }
}
