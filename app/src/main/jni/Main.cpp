#include <list>
#include <vector>
#include <cstring>
#include <pthread.h>
#include <thread>
#include <cstring>
#include <string>
#include <jni.h>
#include <unistd.h>
#include <fstream>
#include <iostream>
#include <dlfcn.h>
#include "Includes/Logger.h"
#include "Includes/obfuscate.h"
#include "Includes/Utils.hpp"
#include "Menu/Menu.hpp"
#include "Menu/Jni.hpp"
#include "Includes/Macros.h"
#include "NxHook.hpp"
#include "NxScanner.hpp" // Custom Signature Scanner

#include <BNM/Loading.hpp>
#include <BNM/Class.hpp>
#include <BNM/Method.hpp>
#include <BNM/Field.hpp>


// Note: Removed heavy libraries (ShadowHook, xHook, xDL) to ensure AIDE build compatibility.
// We are using a custom "NxHook" wrapper around Dobby/KittyMemory which is lightweight and stealthy.

// Universal Features
float timeScale = 1.0f;
float fovValue = 60.0f;
bool fovEnabled = false;

// NxMod Feature List Configuration
jobjectArray GetFeatureList(JNIEnv *env, jobject context) {
    jobjectArray ret;

    const char *features[] = {
            OBFUSCATE("Category_Universal Mods"),
            OBFUSCATE("SeekBar_Time Scale_1_10"),
            OBFUSCATE("SeekBar_FOV Changer_30_120"),
            OBFUSCATE("Category_Misc"),
            OBFUSCATE("ButtonLink_Visit NxMod_https://github.com/NxModTeam")
    };

    int Total_Feature = (sizeof features / sizeof features[0]);
    ret = (jobjectArray)
            env->NewObjectArray(Total_Feature, env->FindClass(OBFUSCATE("java/lang/String")),
                                env->NewStringUTF(""));

    for (int i = 0; i < Total_Feature; i++)
        env->SetObjectArrayElement(ret, i, env->NewStringUTF(features[i]));

    return (ret);
}

void Changes(JNIEnv *env, jclass clazz, jobject obj, jint featNum, jstring featName, jint value, jlong Lvalue, jboolean boolean, jstring text) {
    // 0: TimeScale, 1: FOV
    switch (featNum) {
        case 0:
            timeScale = (float)value;
            // NativeLog(OBFUSCATE("TimeScale set to: ") + std::to_string(timeScale));
            break;
        case 1:
            fovValue = (float)value;
            fovEnabled = true;
            // NativeLog(OBFUSCATE("FOV set to: ") + std::to_string(fovValue));
            break;
    }
}

// Universal Hook: UnityEngine.Time.get_timeScale
float (*old_get_timeScale)();
float get_timeScale() {
    if (timeScale != 1.0f) return timeScale;
    return old_get_timeScale();
}

// Universal Hook: UnityEngine.Camera.get_fieldOfView
// Note: We hook the getter or setter depending on game logic, but often hooking get is safer visual change
float (*old_get_fieldOfView)(void* instance);
float get_fieldOfView(void* instance) {
    if (fovEnabled) return fovValue;
    return old_get_fieldOfView(instance);
}

//Target lib here
#define targetLibName OBFUSCATE("libil2cpp.so")

ElfScanner g_il2cppELF;

// we will run our hacks in a new thread so our while loop doesn't block process main thread
void hack_thread() {
    LOGI(OBFUSCATE("pthread created"));
    NativeLog(OBFUSCATE("[SYSTEM] Initializing NxMod Core..."));

    // NxMod Fix: Added delay to prevent crash on injection by waiting for engine initialization
    // This is crucial for fixing the "crash before game opens" issue.
    sleep(5);

    NativeLog(OBFUSCATE("[BNM] Waiting for il2cpp..."));
    // This loop should be always enabled in unity game
    // because libil2cpp.so is not loaded into memory immediately.
    while (!isLibraryLoaded(targetLibName)) {
        sleep(1); // Wait for target lib be loaded.
    }

    // Init BNM - Universal approach
    // First try JNI if available (requires passing JNIEnv down), or Dlfcn handle.
    // Since we are in a pure C++ thread without JNIEnv attached yet, getting handle is safest.
    void* handle = dlopen("libil2cpp.so", RTLD_LAZY);
    if (handle) {
        if (BNM::Loading::TryLoadByDlfcnHandle(handle)) {
             NativeLog(OBFUSCATE("[BNM] Engine Loaded Successfully."));
        } else {
             NativeLog(OBFUSCATE("[BNM] Failed to initialize BNM hooks. Version mismatch?"));
        }
    } else {
        NativeLog(OBFUSCATE("[BNM] Failed to get libil2cpp.so handle!"));
    }

    do {
        sleep(1);
        g_il2cppELF = ElfScanner::createWithPath(targetLibName);
    } while (!g_il2cppELF.isValid());

    LOGI(OBFUSCATE("%s has been loaded"), (const char *) targetLibName);

#if defined(__aarch64__)
    uintptr_t il2cppBase = g_il2cppELF.base();
    if (il2cppBase == 0) {
        LOGE(OBFUSCATE("Failed to get il2cpp base"));
        return;
    }

    // -------------------------------------------------------------------------
    // UNIVERSAL BNM IMPLEMENTATION
    // -------------------------------------------------------------------------

    // 1. TimeScale Hack
    auto mTimeScale = BNM::Class((const char*)OBFUSCATE("UnityEngine"), (const char*)OBFUSCATE("Time")).GetMethod((const char*)OBFUSCATE("get_timeScale"));
    if (mTimeScale.IsValid()) {
        NativeLog(OBFUSCATE("[SCAN] Found UnityEngine.Time::get_timeScale"));
        NxHook::Install((void*)mTimeScale.GetOffset(), (void*)get_timeScale, (void**)&old_get_timeScale);
        NativeLog(OBFUSCATE("[HOOK] TimeScale Hooked!"));
    } else {
        NativeLog(OBFUSCATE("[SCAN] Failed to find Time.get_timeScale"));
    }

    // 2. FOV Hack
    auto mFOV = BNM::Class((const char*)OBFUSCATE("UnityEngine"), (const char*)OBFUSCATE("Camera")).GetMethod((const char*)OBFUSCATE("get_fieldOfView"));
    if (mFOV.IsValid()) {
        NativeLog(OBFUSCATE("[SCAN] Found UnityEngine.Camera::get_fieldOfView"));
        NxHook::Install((void*)mFOV.GetOffset(), (void*)get_fieldOfView, (void**)&old_get_fieldOfView);
        NativeLog(OBFUSCATE("[HOOK] FOV Hooked!"));
    } else {
        NativeLog(OBFUSCATE("[SCAN] Failed to find Camera.get_fieldOfView"));
    }
    
    NativeLog(OBFUSCATE("[SYSTEM] Universal Hooks Applied. Ready."));

#elif defined(__arm__)
    //Put your code here if you want the code to be compiled for armv7 only
#endif

    LOGI(OBFUSCATE("Done"));
}

__attribute__((constructor))
void lib_main() {
    // Create a new thread to run the hack loop so we don't block the main thread
    std::thread(hack_thread).detach();
}

