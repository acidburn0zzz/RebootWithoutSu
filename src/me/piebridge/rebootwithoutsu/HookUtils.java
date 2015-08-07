package me.piebridge.rebootwithoutsu;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.SystemProperties;

public class HookUtils {

    public static final String TAG = "XposedMotoG";

    public static final String INTENT = XposedMod.class.getPackage() + ".INTENT";

    private static final String ACTION = "ACTION";

    private static final String ACTION_SOFT_REBOOT = "SOFT_REBOOT";

    private static final String ACTION_REBOOT = "REBOOT";

    public static void handleIntent(Context context, Intent intent) {
        String action = intent.getStringExtra(ACTION);
        if (ACTION_SOFT_REBOOT.equals(action)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                SystemProperties.set("ctl.restart", "netd");
            }
            SystemProperties.set("ctl.restart", "surfaceflinger");
            SystemProperties.set("ctl.restart", "zygote");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                SystemProperties.set("ctl.restart", "zygote_secondary");
            }
        } else if (ACTION_REBOOT.equals(action)) {
            SystemProperties.set("sys.powerctl", "reboot");
        }
    }

    private static Intent newIntent(String action) {
        Intent intent = new Intent(INTENT).putExtra(ACTION, action);
        intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        return intent;
    }

    public static Intent newSoftRebootIntent() {
        return newIntent(ACTION_SOFT_REBOOT);
    }

    public static Intent newRebootIntent() {
        return newIntent(ACTION_REBOOT);
    }

}
