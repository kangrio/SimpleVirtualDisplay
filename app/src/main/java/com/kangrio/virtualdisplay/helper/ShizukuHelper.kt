package com.kangrio.virtualdisplay.helper

import android.util.Log
import rikka.shizuku.Shizuku


class ShizukuHelper {
    private val TAG = "ShizukuHelper"
    fun execInternal(command: String) {
        val command = "$command >/dev/null 2>&1 &"
        val commands = arrayOfNulls<String>(3)

        commands[0] = "sh"
        commands[1] = "-c"
        commands[2] = command

        Log.d(TAG, "execInternal: commnad=${commands.toList().toString()}")

        try {
            val process = Shizuku.newProcess(commands, null, null)

            process.waitFor()

        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
}