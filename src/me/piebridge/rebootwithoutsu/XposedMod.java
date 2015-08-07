package me.piebridge.rebootwithoutsu;

import android.app.AndroidAppHelper;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class XposedMod implements IXposedHookZygoteInit, IXposedHookLoadPackage {

    private static boolean systemHooked;

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {

        Class<?> ActivityThread = Class.forName("android.app.ActivityThread");
        XposedBridge.hookAllMethods(ActivityThread, "systemMain", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!systemHooked) {
                    ClassLoader loader = Thread.currentThread().getContextClassLoader();
                    Class<?> PackageManagerService = Class.forName("com.android.server.pm.PackageManagerService", false, loader);
                    XposedHelpers.findAndHookMethod(PackageManagerService, "systemReady", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Context mContext = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                            mContext.registerReceiver(new BroadcastReceiver() {
                                @Override
                                public void onReceive(Context context, Intent intent) {
                                    HookUtils.handleIntent(context, intent);
                                }
                            }, new IntentFilter(HookUtils.INTENT));
                        }
                    });
                    systemHooked = true;
                }
            }
        });

    }

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {

        if ("de.robv.android.xposed.installer".equals(lpparam.packageName)) {
            XC_MethodHook rebootMethodHook = new RebootMethodHook();
            Class<?> RootUtil = XposedHelpers.findClass("de.robv.android.xposed.installer.util.RootUtil", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(RootUtil, "startShell", XC_MethodReplacement.returnConstant(true));
            XposedHelpers.findAndHookMethod(RootUtil, "execute", String.class, List.class, rebootMethodHook);
            XposedHelpers.findAndHookMethod(RootUtil, "executeWithBusybox", String.class, List.class, rebootMethodHook);
        }

    }

    private static class RebootMethodHook extends XC_MethodHook {
        @Override
        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            int result = -1;
            String command = (String) param.args[0];
            Application application = AndroidAppHelper.currentApplication();
            if (command != null && application != null) {
                Context context = application.getApplicationContext();
                if (command.contains("ctl.restart")) {
                    context.sendBroadcast(HookUtils.newSoftRebootIntent());
                    result = 0;
                } else if ("reboot".equals(command)) {
                    context.sendBroadcast(HookUtils.newRebootIntent());
                    result = 0;
                }
            }
            param.setResult(result);
        }
    }

}
