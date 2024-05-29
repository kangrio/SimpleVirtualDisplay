package com.kangrio.virtualdisplay.utils

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.hardware.input.IInputManager
import android.hardware.input.InputManager
import android.util.Log
import android.view.MotionEvent
import android.view.Surface
import com.kangrio.virtualdisplay.App
import com.kangrio.virtualdisplay.MainActivity
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper

class DisplayUtils {
    private val TAG = "DisplayUtils"

    private val iInputManager: IInputManager =
        IInputManager.Stub.asInterface(
            ShizukuBinderWrapper(SystemServiceHelper.getSystemService(Context.INPUT_SERVICE))
        )

    private val handler = App.handler

    fun sendMotionEvent(motionEvent: MotionEvent) {
        iInputManager.injectInputEvent(
            motionEvent,
            InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH
        )
    }


    private var virtualDisplay: VirtualDisplay? = null
    private var displayManager: DisplayManager? = null

    @SuppressLint("WrongConstant")
    fun createVirtualDisplay(
        context: Context,
        surface: Surface?,
        width: Int,
        height: Int
    ): VirtualDisplay {
        var displayId = 0
        var realDensity = context.resources.displayMetrics.densityDpi

        val density: Int =
            realDensity + (0.1f * realDensity).toInt()

        Log.d(TAG, "createVirtualDisplay: density = $density")

        displayManager = DisplayManager(App.applicationContext())

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
            MainActivity.tvDisplayId!!.text = displayId.toString()
        }
        return virtualDisplay as VirtualDisplay
    }
}