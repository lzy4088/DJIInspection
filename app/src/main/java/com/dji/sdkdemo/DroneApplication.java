package com.dji.sdkdemo;

import android.app.Application;
import android.content.Context;
import com.secneo.sdk.Helper;

public class DroneApplication extends Application {
    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Helper.install(DroneApplication.this);
    }
}
