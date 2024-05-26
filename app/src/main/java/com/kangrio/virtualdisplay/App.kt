package com.kangrio.virtualdisplay

import android.app.Application
import android.content.Context
import android.os.Build
import com.kangrio.virtualdisplay.server.helper.FakeContext
import org.lsposed.hiddenapibypass.HiddenApiBypass

class App : Application() {
    init {
        instance = this
    }

    companion object {
        private var instance: App? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        FakeContext.Companion.setContext(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("")
        };
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }
}