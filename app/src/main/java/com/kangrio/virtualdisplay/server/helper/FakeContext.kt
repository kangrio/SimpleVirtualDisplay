package com.kangrio.virtualdisplay.server.helper

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.AttributionSource
import android.content.Context
import android.content.MutableContextWrapper
import android.os.Build
import android.os.Process


class FakeContext private constructor() : MutableContextWrapper(null) {
    companion object {
        const val PACKAGE_NAME: String = "com.android.shell"
        const val ROOT_UID: Int = 0 // Like android.os.Process.ROOT_UID, but before API 29
        private lateinit var context: Context

        @SuppressLint("StaticFieldLeak")
        private val INSTANCE = FakeContext()

        fun get(): FakeContext {
            return INSTANCE
        }

        fun setContext(con: Context) {
            context = con
        }
    }

    override fun getDisplayId(): Int {
        return 0
    }

    override fun enforceCallingOrSelfPermission(permission: String?, message: String?) {
    }

    override fun getPackageName(): String {
        return PACKAGE_NAME
    }

    override fun getOpPackageName(): String {
        return PACKAGE_NAME
    }

    @TargetApi(Build.VERSION_CODES.S)
    override fun getAttributionSource(): AttributionSource {
        val builder = AttributionSource.Builder(Process.SHELL_UID)
        builder.setPackageName(PACKAGE_NAME)
        return builder.build()
    }

    // @Override to be added on SDK upgrade for Android 14
    @Suppress("unused")
    override fun getDeviceId(): Int {
        return 0
    }
}