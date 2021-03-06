package com.RSen.Commandr.core;

import android.accessibilityservice.AccessibilityService;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import com.RSen.Commandr.util.KeyguardUtil;

public class MyAccessibilityService extends AccessibilityService {
    static final String TAG = "accessibility";
    static final long timeOut = 500;
    long lastCommand = 0;
    private static MyAccessibilityService thisService;
    public MyAccessibilityService() {
    }

    public static boolean isAccessibilitySettingsOn(Context mContext) {
        int accessibilityEnabled = 0;
        final String service = "com.RSen.Commandr/com.RSen.Commandr.core.MyAccessibilityService";

        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(), android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "***ACCESSIBILIY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
                splitter.setString(settingValue);
                while (splitter.hasNext()) {
                    String accessabilityService = splitter.next();

                    Log.v(TAG, "-------------- > accessabilityService :: " + accessabilityService);
                    if (accessabilityService.equalsIgnoreCase(service)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v(TAG, "***ACCESSIBILIY IS DISABLED***");
        }

        return false;
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        thisService = this;
        lastCommand = 0;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        KeyguardUtil.unlock(this);
    }

    public static MyAccessibilityService getInstance()
    {
        return thisService;
    }
    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        try {
            String command = accessibilityEvent.getText().get(0).toString();
            if (CommandInterpreter.interpret(this, command, true) && (lastCommand + timeOut) < accessibilityEvent.getEventTime()) {
                lastCommand = accessibilityEvent.getEventTime();


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInterrupt() {

    }
}
