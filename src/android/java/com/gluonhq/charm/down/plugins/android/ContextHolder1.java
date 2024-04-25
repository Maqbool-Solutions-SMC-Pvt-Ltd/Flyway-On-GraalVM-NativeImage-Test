package com.gluonhq.charm.down.plugins.android;

import android.content.Context;

import java.beans.Introspector;

public class ContextHolder1 {
    private ContextHolder1() {}

    private static Context context;

    public static Context getContext() {
        return context;
    }

    public static void setContext(Context context) {
        ContextHolder1.context = context;
    }
}