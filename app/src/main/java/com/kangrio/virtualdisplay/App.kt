package com.kangrio.virtualdisplay

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import org.lsposed.hiddenapibypass.HiddenApiBypass

class App : Application() {
    init {
        instance = this
        val handler = Handler(Looper.getMainLooper())
    }

    @SuppressLint("StaticFieldLeak")
    companion object {
        private var instance: App? = null

        var handler = Handler(Looper.getMainLooper())


        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("")
        };
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }
}