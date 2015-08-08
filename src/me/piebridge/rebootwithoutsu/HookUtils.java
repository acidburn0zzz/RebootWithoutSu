package me.piebridge.rebootwithoutsu;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.widget.Toast;

import java.io.File;
import java.util.Locale;

import de.robv.android.xposed.XposedBridge;

public class HookUtils {

    public static final String TAG = "XposedMotoG";

    public static final String INTENT = XposedMod.class.getPackage() + ".INTENT";

    private static final String ACTION = "ACTION";

    private static final String ACTION_SOFT_REBOOT = "SOFT_REBOOT";

    private static final String ACTION_REBOOT = "REBOOT";

    private static boolean canReboot(Context context) {
        String isa = "arm";
        String abi = SystemProperties.get("ro.product.cpu.abi");
        // armeabi, armeabi-v7a, arm64-v8a, x86, x86_64, mips
        if ("arm64-v8a".equals(abi)) {
            isa = "arm64";
        } else if ("x86".equals(abi) || "x86_64".equals(abi)) {
            isa = abi;
        } else if ("mips".equals(abi) || "mips64".equals(abi)) {
            isa = "mips";
        }
        File booting = new File("/data/dalvik-cache/" + isa + "/.booting");
        XposedBridge.log("checking " + booting + " for abi " + abi);
        if (booting.exists()) {
            Toast.makeText(context, getMessage(), Toast.LENGTH_LONG).show();
            return false;
        } else {
            return true;
        }
    }

    private static String getMessage() {
        if (Locale.CHINA.equals(Locale.getDefault())) {
            return "发现文件'.booting', 请稍后再重启";
        } else {
            return "found .booting, please reboot later";
        }
    }


    public static boolean handleIntent(Context context, Intent intent) {
        if (!canReboot(context)) {
            return false;
        }
        String action = intent.getStringExtra(ACTION);
        if (ACTION_SOFT_REBOOT.equals(action)) {
            SystemProperties.set("ctl.restart", "surfaceflinger");
        } else if (ACTION_REBOOT.equals(action)) {
            SystemProperties.set("sys.powerctl", "reboot");
        }
        return true;
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
