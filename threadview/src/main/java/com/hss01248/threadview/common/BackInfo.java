package com.hss01248.threadview.common;

import java.lang.ref.WeakReference;

public class BackInfo {
    public String activity;
    public StackTraceElement[] createStack;
    public WeakReference obj;
    public Class clazz;
}
