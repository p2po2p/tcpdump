package com.p2po2p.tcpdump;

import android.annotation.TargetApi;
import android.app.Application;
import android.os.StrictMode;

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //8.0适配，StrictMode适配，共享文件相关；
        checkStrictMode();
    }

    @TargetApi(24)
    private void checkStrictMode() {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
    }
}
