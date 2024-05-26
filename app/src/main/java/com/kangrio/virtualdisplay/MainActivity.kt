package com.kangrio.virtualdisplay

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.SurfaceView
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kangrio.virtualdisplay.server.helper.services.IUserService
import com.kangrio.virtualdisplay.server.helper.services.UserService
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.OnRequestPermissionResultListener
import rikka.shizuku.Shizuku.UserServiceArgs


class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"

    private var mIUserService: IUserService? = null
    private var displayManager: DisplayManager? = null
    private var virtualDisplay: VirtualDisplay? = null

    private val testPackage = "com.android.settings"

    private fun onRequestPermissionsResult(requestCode: Int, grantResult: Int) {
        val granted = grantResult == PackageManager.PERMISSION_GRANTED
    }

    private val REQUEST_PERMISSION_RESULT_LISTENER =
        OnRequestPermissionResultListener { requestCode: Int, grantResult: Int ->
            this.onRequestPermissionsResult(
                requestCode, grantResult
            )
        }
    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
            if (binder.pingBinder()) {
                mIUserService = IUserService.Stub.asInterface(binder)
                Log.d(TAG, "onServiceConnected: binded")
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mIUserService = null
        }
    }
    private val userServiceArgs: UserServiceArgs = UserServiceArgs(
        ComponentName(
            BuildConfig.APPLICATION_ID, UserService::class.java.name
        )
    ).daemon(false).processNameSuffix("user_service").debuggable(BuildConfig.DEBUG)
        .version(BuildConfig.VERSION_CODE)


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER)

        checkPermission(1)


        Handler().postDelayed(Runnable {
            bindUserService()
            createVirtualDisplay()

        }, 3000L)

        Handler().postDelayed(Runnable {
            launchAppsTargetDisplay(
                testPackage, displayManager!!.displays.last().displayId
            )
        }, 5000L)

        findViewById<Button>(R.id.button).setOnClickListener {
            launchAppsTargetDisplay(
                "bin.mt.plus", displayManager!!.displays.last().displayId
            )
        }

    }

    @SuppressLint("WrongConstant")
    fun createVirtualDisplay() {
        var displayId = 0
        val surfaceView = findViewById<SurfaceView>(R.id.surfaceView)
        val holder = surfaceView.holder

        displayManager = DisplayManager(this)
        if (displayManager!!.displays.size > 1) {
            if (displayManager!!.displays.last().ownerPackageName.equals(BuildConfig.APPLICATION_ID)) {
                displayId = displayManager!!.displays.last().displayId
            } else {
                virtualDisplay = displayManager!!.createVirtualDisplay(
                    "MyVirtualDisplay", 800, 600, 240, holder.surface, 2
                )
                displayId = virtualDisplay!!.display.displayId
                Log.d(TAG, "createVirtualDisplayTest: Succes: $displayId")
                Log.d(
                    TAG,
                    "createVirtualDisplayTest: Succes: $displayId: ${virtualDisplay!!.display.flags}"
                )
            }
        } else {
            virtualDisplay = displayManager!!.createVirtualDisplay(
                "MyVirtualDisplay", 800, 600, 240, holder.surface, 2
            )
            displayId = virtualDisplay!!.display.displayId
            Log.d(TAG, "createVirtualDisplayTest: Succes: $displayId")
            Log.d(
                TAG,
                "createVirtualDisplayTest: Succes: $displayId: ${virtualDisplay!!.display.flags}"
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("WrongConstant", "ServiceCast")
    fun launchAppsTargetDisplay(packageName: String, displayId: Int) {
        Log.d(TAG, "launchAppsTargetDisplay: Launching App")
        var componentClassName: String =
            packageManager.getLaunchIntentForPackage(packageName).component.className

        try {
            mIUserService?.launchApp(packageName, componentClassName, displayId)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        virtualDisplay!!.release()
        unBindUserService()
        Shizuku.removeRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER)
    }

    private fun checkPermission(code: Int): Boolean {
        if (Shizuku.isPreV11()) {
            // Pre-v11 is unsupported
            return false
        }

        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            // Granted
            return true
        } else if (Shizuku.shouldShowRequestPermissionRationale()) {
            // Users choose "Deny and don't ask again"
            return false
        } else {
            // Request the permission
            Shizuku.requestPermission(code)
            return false
        }
    }

    fun bindUserService() {
        if (Shizuku.getVersion() >= 10) {
            Shizuku.bindUserService(userServiceArgs, connection)
        } else {
        }
    }

    fun unBindUserService() {
        if (Shizuku.getVersion() >= 10) {
            Shizuku.unbindUserService(userServiceArgs, connection, true)
        }
    }
}