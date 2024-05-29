package com.kangrio.virtualdisplay.utils

import android.app.ActivityOptions
import android.app.IActivityManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.IPackageManager
import android.os.Build
import android.util.Log
import com.kangrio.virtualdisplay.helper.ShizukuHelper
import com.kangrio.virtualdisplay.server.helper.FakeContext
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper

class AppsUtils {
    val TAG = "AppsUtils"
    val shizukuHelper: ShizukuHelper = ShizukuHelper()

    val iPackageManager: IPackageManager =
        IPackageManager.Stub.asInterface(ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package")))

//    val packageManager: PackageManager = PackageManager::class.java.getConstructor().newInstance()

    val iActivityManager: IActivityManager = IActivityManager.Stub.asInterface(
        ShizukuBinderWrapper(
            SystemServiceHelper.getSystemService("activity")
        )
    )

    fun launchAppTargetDisplay(packageName: String, componentClassName: String, displayId: Int) {
        Log.d(TAG, "launchAppTargetDisplay: $displayId")

        val i = Intent(packageName)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        i.component = ComponentName(packageName, componentClassName)

        val activityOptions: ActivityOptions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ActivityOptions.makeBasic().setLaunchDisplayId(displayId)
        } else {
            ActivityOptions.makeBasic()
        }

        iActivityManager.startActivity(
            null,
            FakeContext.PACKAGE_NAME,
            i,
            null,
            null,
            null,
            0,
            Intent.FLAG_ACTIVITY_NEW_TASK,
            null,
            activityOptions.toBundle()
        )

        /*
        val cmd = "am start -n $packageName/$componentClassName --display $displayId"
        Log.d(TAG, "launchAppTargetDisplay: $cmd")
        try {
            shizukuHelper.execInternal(cmd)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
         */
    }

    fun killApp(packageName: String) {
        val cmd = "am force-stop $packageName"
        Log.d(TAG, "killApp: $cmd")
        try {
            shizukuHelper.execInternal(cmd)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}