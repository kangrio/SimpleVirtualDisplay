package com.kangrio.virtualdisplay.utils

import android.app.ActivityOptions
import android.app.IActivityManager
import android.app.Presentation
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.IPackageManager
import android.os.Build
import android.util.Log
import android.view.Display
import com.kangrio.virtualdisplay.helper.ShizukuHelper
import com.kangrio.virtualdisplay.server.helper.FakeContext
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper

class AppsUtils {
    val TAG = "AppsUtils"
    val shizukuHelper: ShizukuHelper = ShizukuHelper()

    val iPackageManager: IPackageManager =
        IPackageManager.Stub.asInterface(ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package")))

    val iActivityManager: IActivityManager = IActivityManager.Stub.asInterface(
        ShizukuBinderWrapper(
            SystemServiceHelper.getSystemService("activity")
        )
    )

    fun launchAppTargetDisplay(packageName: String, componentClassName: String, displayId: Int) {
        Log.d(TAG, "launchAppTargetDisplay: $displayId")


        val i = Intent(packageName)
        i.addFlags(Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS)
        i.component = ComponentName(packageName, componentClassName)

        val activityOptions: ActivityOptions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ActivityOptions.makeBasic().setLaunchDisplayId(displayId)
        } else {
            ActivityOptions.makeBasic()
        }

        activityOptions.launchWindowingMode = 1

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


//        val prsentations =
//            MyPresentation(App.applicationContext(), displayManager.getDisplay(displayId))
//
//
//        prsentations.show()

//        var tasks = iActivityTaskManager.moveRootTaskToDisplay(10383, displayId)
//
//        Log.d(TAG, "launchAppTargetDisplay: ${tasks}")

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

private class MyPresentation(context: Context?, display: Display?) :
    Presentation(context, display) {

}