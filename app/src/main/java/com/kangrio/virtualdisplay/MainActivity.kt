package com.kangrio.virtualdisplay

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.kangrio.virtualdisplay.utils.AppsUtils
import com.kangrio.virtualdisplay.utils.DisplayUtils
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.OnRequestPermissionResultListener
import java.util.SortedMap

class MainActivity : AppCompatActivity() {
    @SuppressLint("StaticFieldLeak")
    companion object {
        var tvDisplayId: TextView? = null
    }

    private val TAG = "MainActivity"

    private val appsUtils = AppsUtils()
    private val displayUtils = DisplayUtils()

    private val handler = App.handler

    var surfaceView: SurfaceView? = null

    private var spinner: Spinner? = null

    private var displayManager: DisplayManager? = null
    private var virtualDisplay: VirtualDisplay? = null

    private val testPackage = "com.android.settings"
    private var lastOpenedApp = testPackage


    private var btnStartApp: Button? = null
    private var btnKillApp: Button? = null

    private var editText: EditText? = null

    @RequiresApi(Build.VERSION_CODES.O)
    private fun onRequestPermissionsResult(requestCode: Int, grantResult: Int) {
        val granted = grantResult == PackageManager.PERMISSION_GRANTED
        Log.d(TAG, "onRequestPermissionsResult: $granted")
        if (granted) {
            createSurface()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val REQUEST_PERMISSION_RESULT_LISTENER =
        OnRequestPermissionResultListener { requestCode: Int, grantResult: Int ->
            this.onRequestPermissionsResult(
                requestCode, grantResult
            )
        }

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
        displayManager = DisplayManager(this)

        btnStartApp = findViewById(R.id.btnStartApp)
        btnKillApp = findViewById(R.id.btnKillApp)
        editText = findViewById(R.id.editTextText)

        surfaceView = findViewById(R.id.surfaceView)
        spinner = findViewById(R.id.planets_spinner)

        tvDisplayId = findViewById(R.id.tvDisplayId)



        if (savedInstanceState == null) {
            if (checkPermission(1)) {
                handler.postDelayed({
                    createSurface()
                }, 500L)
            }
        } else {
            Log.d(TAG, "Welcome back.")
        }


        btnStartApp!!.setOnClickListener {
            launchAppsTargetDisplay(
                editText!!.text.toString(), virtualDisplay!!.display.displayId
            )
        }

        btnKillApp!!.setOnClickListener {
            appsUtils.killApp(editText!!.text.toString())
        }

        val pm = packageManager

        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(intent, PackageManager.GET_META_DATA)

        val installedApps: HashMap<String, String> = HashMap()
        var sortedInstalledApps: SortedMap<String, String>? = null

        handler.post {
            for (packageInfo in apps) {
                try {
                    installedApps[packageInfo.loadLabel(pm).toString()] = packageInfo.activityInfo.packageName
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
            sortedInstalledApps = installedApps.toSortedMap()
            Log.d(TAG, "onCreate: ${sortedInstalledApps.toString()}")

            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                sortedInstalledApps!!.keys.toList()
            )
            spinner!!.setAdapter(adapter)

            spinner!!.setOnItemClickListenerInt(OnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
                val packageName = sortedInstalledApps!!.values.toList()[position]
                editText!!.setText(packageName)
                val app: PackageInfo

                try {
                    app = pm.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
                } catch (e: PackageManager.NameNotFoundException) {
                    throw RuntimeException(e)
                }

                val requestedPermissions =
                    app.requestedPermissions ?: // No permissions defined in <manifest>
                    return@OnItemClickListener

                val index = listOf(*requestedPermissions)
                    .indexOf(Manifest.permission.WRITE_SECURE_SETTINGS)

                if (index == -1) return@OnItemClickListener
            })

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
        if (virtualDisplay != null) virtualDisplay!!.release()

        Shizuku.removeRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER)
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: ")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: ")
    }

    @SuppressLint("WrongConstant", "ServiceCast")
    fun launchAppsTargetDisplay(packageName: String, displayId: Int) {
        lastOpenedApp = packageName

        try {

            val componentClassName: String =
                packageManager.getLaunchIntentForPackage(packageName).component.className

            val appsUtils: AppsUtils = AppsUtils()

            appsUtils.launchAppTargetDisplay(packageName, componentClassName, displayId)
        } catch (e: Throwable) {
            e.printStackTrace()
        }

    }

    fun getExternalPackageName(): List<PackageInfo>? {
        val packageNames = packageManager.getInstalledPackages(0) as? List<PackageInfo>
        // Check if packageNames is not null before accessing its elements
        if (!packageNames.isNullOrEmpty()) {
            return packageNames
        }
        return null
    }

    private fun sendMotionEvent(motionEvent: MotionEvent) {
        try {
            displayUtils.sendMotionEvent(motionEvent)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    @SuppressLint("WrongConstant")
    fun createVirtualDisplay(surface: Surface?, width: Int, height: Int) {
        var displayId = 0
        val density: Int =
            resources.displayMetrics.densityDpi + (0.2f * resources.displayMetrics.densityDpi).toInt()

        Log.d(TAG, "createVirtualDisplay: density = $density")

        if (virtualDisplay != null) virtualDisplay!!.release()

        virtualDisplay = displayManager!!.createVirtualDisplay(
            "MyVirtualDisplay",
            width,
            height,
            density,
            surface,
            2
        )

        displayId = virtualDisplay!!.display.displayId
        Log.d(TAG, "createVirtualDisplayTest: Succes: len(${displayManager!!.displays.size})")
        Log.d(
            TAG,
            "createVirtualDisplayTest: Succes: $displayId: ${virtualDisplay!!.display.flags}"
        )

        handler.post {
            tvDisplayId!!.text = "Display ID: $displayId"
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun createSurface() {
        surfaceView!!.visibility = View.VISIBLE


        val holder = surfaceView!!.holder

        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder?) {
                Log.d(TAG, "surfaceCreated: ${holder!!.surface.isValid}")
                virtualDisplay = displayUtils.createVirtualDisplay(
                    this@MainActivity,
                    holder.surface,
                    surfaceView!!.width,
                    surfaceView!!.height
                )
                if (lastOpenedApp.isNotEmpty()) {
                    launchAppsTargetDisplay(lastOpenedApp, virtualDisplay!!.display.displayId)
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder?, format: Int, width: Int, height: Int
            ) {
                Log.d(TAG, "surfaceChanged: ${holder!!.surface.isValid}")
            }

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                virtualDisplay!!.release()
                Log.d(TAG, "surfaceDestroyed: ${holder!!.surface.isValid}")
            }
        })

        surfaceView!!.setOnTouchListener { view, motionEvent ->
            motionEvent.displayId = virtualDisplay!!.display.displayId
            sendMotionEvent(motionEvent)
            true
        }
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
}