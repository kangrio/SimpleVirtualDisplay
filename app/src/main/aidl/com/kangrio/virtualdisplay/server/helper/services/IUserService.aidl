// IUserService.aidl
package com.kangrio.virtualdisplay.server.helper.services;

// Declare any non-default types here with import statements

interface IUserService {
    void destroy() = 16777114; // Destroy method defined by Shizuku server

//    void createVirtualDisplay(in Surface surface) = 0;

    void launchApp(String pkg, String packageName, int displayId) = 13;
}