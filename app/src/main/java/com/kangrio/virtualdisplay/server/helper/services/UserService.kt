package com.kangrio.virtualdisplay.server.helper.services

import android.content.pm.IPackageManager
import android.util.Log
import rikka.shizuku.SystemServiceHelper


class UserService : IUserService.Stub() {
    var TAG = "UserService"

    val iPackageManager: IPackageManager =
        IPackageManager.Stub.asInterface(SystemServiceHelper.getSystemService("package"))

    override fun destroy() {

    }

    override fun launchApp(packageName: String, componentClassName: String, displayId: Int) {
        Log.d("TAG", "launchAp1111: $packageName")

        val rt = Runtime.getRuntime()
        try {
//            rt.exec("am start -n $pkg/.$componentClassName --display $displayId")
            rt.exec("am start -n $packageName/$componentClassName --display $displayId")
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}