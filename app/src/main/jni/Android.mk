LOCAL_PATH := $(call my-dir)

# --------------------------------------------------------
# 1. Prebuilt Libraries (Keystone & Dobby)
# --------------------------------------------------------
include $(CLEAR_VARS)
LOCAL_MODULE    := Keystone
# Pastikan path ini benar ada di folder projectmu
LOCAL_SRC_FILES := KittyMemory/Deps/Keystone/libs-android/$(TARGET_ARCH_ABI)/libkeystone.a
include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := Dobby
# Pastikan path ini benar ada di folder projectmu
LOCAL_SRC_FILES := Dobby/$(TARGET_ARCH_ABI)/libdobby.a
include $(PREBUILT_STATIC_LIBRARY)

# --------------------------------------------------------
# 2. Main Mod Library (NxMod)
# --------------------------------------------------------
include $(CLEAR_VARS)
LOCAL_MODULE    := NxMod

# --- Source Files ---
# Saya rapikan urutannya agar mudah dibaca
LOCAL_SRC_FILES := \
    Main.cpp \
    Includes/Utils.cpp \
    Menu/Jni.cpp \
    Menu/Menu.cpp \
    Menu/Setup.cpp \
    Substrate/hde64.c \
    Substrate/SubstrateDebug.cpp \
    Substrate/SubstrateHook.cpp \
    Substrate/SubstratePosixMemory.cpp \
    Substrate/SymbolFinder.cpp \
    KittyMemory/KittyArm64.cpp \
    KittyMemory/KittyScanner.cpp \
    KittyMemory/KittyMemory.cpp \
    KittyMemory/KittyUtils.cpp \
    KittyMemory/MemoryPatch.cpp \
    KittyMemory/MemoryBackup.cpp \
    And64InlineHook/And64InlineHook.cpp

# --- Include Paths ---
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_C_INCLUDES += $(LOCAL_PATH)/Includes
LOCAL_C_INCLUDES += $(LOCAL_PATH)/Dobby
LOCAL_C_INCLUDES += $(LOCAL_PATH)/BNM/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/BNM/external/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/BNM/external/utf8

# --- Compiler Flags (Optimized for NDK r28) ---
# C Flags (untuk file .c)
LOCAL_CFLAGS := -O3 -fvisibility=hidden -Wall -Wextra -Wno-unused-parameter

# C++ Flags (untuk file .cpp)
# -std=c++20: Standar modern yang didukung baik oleh NDK r28 (Required for BNM)
# -fpermissive: Wajib ada jika source code Substrate/And64 kamu code lama
# -Wno-*: Mematikan warning rewel dari Clang agar tidak dianggap error
LOCAL_CPPFLAGS := -std=c++20 -O3 -fvisibility=hidden \
    -fpermissive -fexceptions -frtti \
    -Wno-error=format-security \
    -Wno-error=c++11-narrowing \
    -Wno-deprecated-declarations \
    -Wno-unused-variable

# --- Linker Flags ---
# --gc-sections: Membuang fungsi yang tidak terpakai (memperkecil ukuran lib)
# --strip-all: Menghapus simbol debug (agar susah di-reverse engineer)
LOCAL_LDFLAGS += -Wl,--gc-sections,--strip-all

# --- System Libraries ---
LOCAL_LDLIBS := -llog -landroid -lEGL -lGLESv2

# --- Static Libraries Links ---
LOCAL_STATIC_LIBRARIES := Keystone Dobby BNM

include $(BUILD_SHARED_LIBRARY)

# --------------------------------------------------------
# 3. Include BNM Subproject
# --------------------------------------------------------
include $(LOCAL_PATH)/BNM/Android.mk
