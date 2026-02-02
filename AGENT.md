# Panduan Lengkap NxMod: Modding Universal Unity (Android)

## 1. Pendahuluan
NxMod telah direvamp menjadi alat modding "Super Power" dengan antarmuka Cyberpunk dan integrasi mesin BNM (ByNameModding) untuk kompatibilitas universal terhadap game Unity (Il2Cpp).

### Fitur Utama
*   **Universal Unity Support:** Mendeteksi `libil2cpp.so` secara otomatis.
*   **Hacker UI:** Tampilan modern dengan Real-time Console Log.
*   **BNM Engine:** Tidak perlu update offset manual setiap game update (Dynamic Resolution).
*   **Safe Mode:** Deteksi otomatis jika engine gagal load.

---

## 2. Cara Kerja Sistem
1.  **Injection:** Saat game dimulai, library `libNxMod.so` dimuat ke memori.
2.  **Initialization:**
    *   Thread baru dibuat (`hack_thread`).
    *   Sistem menunggu `libil2cpp.so` (Library Game Unity) dimuat.
    *   **BNM Loader** mencoba mengaitkan diri ke engine Unity untuk membaca Class, Method, dan Field secara dinamis.
3.  **UI Overlay:** Service Java (`Launcher.java`) menampilkan Floating Menu di atas game.
4.  **Komunikasi:** Native C++ mengirim log aktivitas ke Java UI via JNI (`nativeLog`).

---

## 3. Panduan Inject (PENTING)

Agar mod menu berjalan, Anda harus memuat library `NxMod` ke dalam proses game. Ada beberapa metode injeksi Smali yang bisa digunakan.

### Target: `classes.dex` (Smali)

Buka file APK game menggunakan `APKEditor` atau `MT Manager`, lalu decompile `classes.dex`. Cari lokasi yang tepat untuk menyisipkan kode pemuat.

#### Metode A: `OnCreate` (Rekomendasi - Paling Stabil)
Cari Activity utama game (biasanya `com.unity3d.player.UnityPlayerActivity` atau lihat di AndroidManifest.xml).
Cari method `onCreate`. Tambahkan kode ini di baris pertama method:

```smali
const-string v0, "NxMod"
invoke-static {v0}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V
```

#### Metode B: `attachBaseContext` (Untuk load lebih awal)
Jika `onCreate` terdeteksi atau terlalu lambat, gunakan `attachBaseContext` di Application Class atau Activity utama.

```smali
.method protected attachBaseContext(Landroid/content/Context;)V
    .locals 1
    invoke-super {p0, p1}, Landroid/app/Activity;->attachBaseContext(Landroid/content/Context;)V
    const-string v0, "NxMod"
    invoke-static {v0}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V
    return-void
.end method
```

#### Metode C: `clinit` (Constructor Statis - Paling Awal)
Hanya gunakan jika Anda tahu apa yang Anda lakukan. Ini berjalan saat class pertama kali dimuat.

```smali
.method static constructor <clinit>()V
    .locals 1
    const-string v0, "NxMod"
    invoke-static {v0}, Ljava/lang/System;->loadLibrary(Ljava/lang/String;)V
    return-void
.end method
```

---

## 4. Troubleshooting Crash

Jika game Force Close (FC) / Crash setelah injeksi:

### A. Versi Unity Tidak Cocok
BNM perlu tahu perkiraan versi Unity. Default saat ini adalah **Unity 2020.3.x**.
Jika game Anda sangat baru (2022+) atau sangat lama (2018), ubah di `app/src/main/jni/BNM/include/BNM/UserSettings/GlobalSettings.hpp`:

```cpp
// Pilih salah satu yang sesuai:
// #define UNITY_VER 182 // Unity 2018
// #define UNITY_VER 194 // Unity 2019
#define UNITY_VER 203 // Unity 2020 (Default Aman)
// #define UNITY_VER 222 // Unity 2022
```
Setelah diubah, **Rebuild Library**.

### B. Proteksi Game (Anti-Cheat)
*   **Metadata Check:** Game mungkin mengecek integritas `global-metadata.dat`. NxMod tidak menyentuh file ini, tapi injeksi hook bisa terdeteksi.
*   **ExtractNativeLibs:** Pastikan di `AndroidManifest.xml` tag `<application>` memiliki atribut:
    `android:extractNativeLibs="true"`
    Ini penting agar `libNxMod.so` bisa dibaca path-nya oleh `dlopen`.

### C. Arsitektur Salah
Pastikan Anda menginjeksi library yang sesuai dengan arsitektur HP/Game.
*   Game 64-bit (arm64-v8a) -> Pakai `libNxMod.so` folder `arm64-v8a`.
*   Game 32-bit (armeabi-v7a) -> Pakai `libNxMod.so` folder `armeabi-v7a`.
Jangan campur aduk!

---

## 5. Build Environment
Disarankan menggunakan:
*   AndroidIDE / AIDE
*   NDK r28 (Support C++20)
*   Pastikan submodul `BNM` dan `Dobby` ada di folder `jni`.
