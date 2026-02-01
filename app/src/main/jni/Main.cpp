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


// Note: Removed heavy libraries (ShadowHook, xHook, xDL) to ensure AIDE build compatibility.
// We are using a custom "NxHook" wrapper around Dobby/KittyMemory which is lightweight and stealthy.

bool noDeath;
int scoreMul = 1, coinsMul = 1;

struct MemPatches {
    MemoryPatch noDeath;
} gPatches;

// NxMod Feature List Configuration
jobjectArray GetFeatureList(JNIEnv *env, jobject context) {
    jobjectArray ret;

    const char *features[] = {
            OBFUSCATE("Category_Cheats"),
            OBFUSCATE("Toggle_God Mode"),
            OBFUSCATE("Button_Start Invincibility (30s)"),
            OBFUSCATE("SeekBar_Score Multiplier_1_100"),
            OBFUSCATE("SeekBar_Coins Multiplier_1_1000"),
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

bool btnPressed = false;

void Changes(JNIEnv *env, jclass clazz, jobject obj, jint featNum, jstring featName, jint value, jlong Lvalue, jboolean boolean, jstring text) {

    switch (featNum) {
        case 0:
        {
            if (boolean)
                gPatches.noDeath.Modify();
            else
                gPatches.noDeath.Restore();
            break;
        }
        case 1:
            btnPressed = true;
            break;
        case 2:
            scoreMul = value;
            break;
        case 3:
            coinsMul = value;
            break;
    }
}

//CharacterPlayer
void (*StartInvcibility)(void *instance, float duration);

void (*old_Update)(void *instance);

void Update(void *instance) {
    if (instance != nullptr) {
        if (btnPressed) {
            StartInvcibility(instance, 30);
            btnPressed = false;
        }
    }
    return old_Update(instance);
}

void (*old_AddScore)(void *instance, int score);
void AddScore(void *instance, int score) {
    return old_AddScore(instance, score * scoreMul);
}

void (*old_AddCoins)(void *instance, int count);
void AddCoins(void *instance, int count) {
    return old_AddCoins(instance, count * coinsMul);
}

//Target lib here
#define targetLibName OBFUSCATE("libil2cpp.so")

ElfScanner g_il2cppELF;

// we will run our hacks in a new thread so our while loop doesn't block process main thread
void hack_thread() {
    LOGI(OBFUSCATE("pthread created"));

    // NxMod Fix: Added delay to prevent crash on injection by waiting for engine initialization
    // This is crucial for fixing the "crash before game opens" issue.
    sleep(5);

    // This loop should be always enabled in unity game
    // because libil2cpp.so is not loaded into memory immediately.
    while (!isLibraryLoaded(targetLibName)) {
        sleep(1); // Wait for target lib be loaded.
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

    //Il2Cpp: Use RVA offset
    StartInvcibility = (void (*)(void *, float)) getAbsoluteAddress(targetLibName, str2Offset(
            OBFUSCATE("0x107A3BC")));

    // -------------------------------------------------------------------------
    // LIBRARY 1: NxScanner (Custom Signature Scanning)
    // -------------------------------------------------------------------------
    uintptr_t scanResult = NxScanner::Scan(il2cppBase, 1024 * 1024 * 40, "C0 03 5F D6");
    if (scanResult != 0) {
        LOGI(OBFUSCATE("Found signature at: %p"), (void*)scanResult);
    }

    // -------------------------------------------------------------------------
    // HOOKING IMPLEMENTATION (NxHook -> Unique Stealth Dobby Wrapper)
    // -------------------------------------------------------------------------
    // Hook Score
    NxHook::Install((void *)getAbsoluteAddress(targetLibName, str2Offset(OBFUSCATE("0x107A2E0"))), (void *)AddScore, (void **)&old_AddScore);
    
    // Hook Coins
    NxHook::Install((void *)getAbsoluteAddress(targetLibName, str2Offset(OBFUSCATE("0x107A2FC"))), (void *)AddCoins, (void **)&old_AddCoins);
    
    // Hook Update
    NxHook::Install((void *)getAbsoluteAddress(targetLibName, str2Offset(OBFUSCATE("0x1078C44"))), (void *)Update, (void **)&old_Update);

    // Memory Patching (NoDeath) using KittyMemory
    gPatches.noDeath = MemoryPatch::createWithHex(il2cppBase + str2Offset(OBFUSCATE("0x1079728")), "C0 03 5F D6");

#elif defined(__arm__)
    //Put your code here if you want the code to be compiled for armv7 only
#endif

    LOGI(OBFUSCATE("Done"));
}

__attribute__((constructor))
void lib_main() {
    std::thread(hack_thread).detach();
}

