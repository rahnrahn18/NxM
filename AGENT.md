Berikut adalah dokumentasi teknis arsitektur untuk integrasi alat modding dan pengembangan visualisasi real-time pada proyek NxMod.
Dokumentasi ini disusun berdasarkan pedoman Enviroment pengguna.
‎***
‎[All Enviroment & tools Installed in my Enviroment]
‎
‎IDE & ENV
‎IDE : Android Code Studio / AndroidIDE (AndroidCSOfficial v1.0.0+gh.r3)
‎Platform : Android (on-device)
‎Device ABI : arm64-v8a
‎Java Runtime (JDK): OpenJDK 17.0.16
‎Gradle Wrapper 8.13-bin.zip
‎
‎Core Tools:
‎  - Cmdline-Tools: 20.0 (latest)
‎  - Platform-Tools: 34.0.4 (adb, fastboot)
‎  - Patcher: v4
‎Build Configuration:
‎  - Build-Tools Versions: 
‎    - 35.0.0
‎- Kotlin 2.1.0
‎    compileSdk 35
‎    buildToolsVersion "35.0.0" // wajib
‎    ndkVersion "28.2.13676358" // wajib
‎      - CMake Version: 4.1.1 (wajib)
‎  - Build System: Ninja (Implicitly supported by CMake suite)
‎
‎---
‎Penting !
‎location android-sdk in my enviroment: "/data/user/0/com.tom.rv2ide/files/home/android-sdk/"
‎Karena CompileSdk 35 (Android 15) dan Build-Tools 35.0.0, pastikan di file build.gradle (Project Level) atau libs.versions.toml, versi Plugin Android kamu minimal 8.4.0 atau lebih baru (8.5/8.6 recommended).
‎---
‎***

Dokumentasi Arsitektur Teknis: Integrasi NxMod & Visualisasi Realtime
Proyek: NxMod (Base LGLTeam)
Environment: Android Native (AndroidIDE), ARM64
Status Saat Ini: Build Stable, dobby.h, libdobby.a, And64InlineHook, KittyMemory, Substrate, Inclue: looger.h, Macros.h, Utils.cpp, get_device_api_level_inlines.h, obfusecate.h, keystone.a dan masih banyak lagi telah Integrated.✅

- Untuk mengintegrasikan pemantauan jaringan PCAPdroid ke dalam proyek NxMod. remote_capture ini relevan untuk membuat jembatan JNI guna menangani data yang diterima dari mesin asli. 

1. Konsep Dasar Integrasi Sistem
Tujuan utama pembaruan ini adalah mengubah metode modding dari Static Offset menjadi Dynamic Resolution dan menambahkan lapisan keamanan data server, serta antarmuka visualisasi aktivitas sistem (Hacker Log Console).
Alur kerja sistem baru ini menggabungkan tiga komponen utama dari referensi yang diberikan:
 * Core Logic (C++): Tempat logika permainan dan hooking.
 * Resolution Engine (BNM): Alat pemindaian memori berbasis nama untuk otomatisasi offset.
 * Validation Engine (Hash-Library): Alat manipulasi checksum untuk validasi server.
 * UI Bridge (JNI): Jembatan pengirim data status ke tampilan visual pengguna.
2. Modul Resolusi Memori Dinamis (Integrasi BNM)
Mengacu pada referensi alat BNM-Android (ByNameModding), modul ini berfungsi sebagai "otak pencari" yang menggantikan pencarian manual.
Logika Koneksi & Alur Kerja:
 * Inisialisasi Runtime: Saat library dimuat, BNM tidak menggunakan alamat memori statis (hex). Ia bekerja dengan memindai struktur internal game saat game berjalan.
 * Pencarian Berbasis Nama:
   * Sistem NxMod mengirimkan perintah string (misal: cari method get_Money atau class PlayerData) ke modul BNM.
   * BNM menelusuri memori secara otomatis untuk menemukan lokasi fungsi tersebut. Ini mencegah mod menjadi usang (expired) saat game melakukan update, karena nama fungsi jarang berubah dibandingkan alamat offsetnya.
 * Handover ke tools pendukung yang relevan: Setelah BNM menemukan alamat target, alamat tersebut diserahkan ke salah satu Hooking Framework (yang relevan dan sudah terintegrasi di sistem) untuk melakukan intersepsi atau pembajakan fungsi.
3. Modul Validasi Server & Integritas Data
Mengacu pada referensi Hash-Library (Chocobo1) dan XXHash, modul ini berfungsi untuk memanipulasi protokol komunikasi agar modifikasi data dianggap valid oleh server.
Logika Koneksi & Alur Kerja:
 * Intersepsi Paket: tools yang tersedia digunakan untuk menahan fungsi pengiriman data jaringan (seperti send()) sebelum data meninggalkan perangkat.
 * Modifikasi & Re-Hashing:
   * Setelah data permainan diubah (misal: currency ditambah), data tersebut menjadi "korup" di mata server karena checksum-nya salah.
   * Sistem NxMod memanggil Hash-Library untuk menghitung ulang checksum (MD5, SHA-256, atau SHA-3) dari data yang sudah dimodifikasi.
   * Jika game menggunakan validasi integritas memori yang sangat cepat, modul XXHash digunakan karena kecepatannya yang tinggi.
 * Pengiriman Valid: Paket data yang sudah dimodifikasi dan ditandatangani ulang dengan hash baru dikirim ke server, membuat server menerima data tersebut sebagai data asli.
4. Sistem Visualisasi Log Realtime (Hacker UI)
Untuk memenuhi permintaan mengenai visualisasi teks berjalan yang menunjukkan sistem sedang bekerja (hacker style), diperlukan jalur komunikasi antara layer C++ (Native) dan layer Java/Kotlin (UI AndroidIDE).
Arsitektur Alur Data (Pipeline):
 * Event Trigger (Sumber Data - C++):
   Setiap kali komponen di atas bekerja, sistem akan memicu sinyal:
   * Saat BNM menemukan alamat -> Sinyal SCAN_SUCCESS.
   * Saat Tools yang telah terintergrasi melakukan hook -> Sinyal HOOK_ATTACHED.
   * Saat Hash-Lib memvalidasi paket -> Sinyal PACKET_SIGNED.
 * JNI Bridge (Jembatan Komunikasi):
   Karena C++ tidak bisa langsung menggambar ke layar HP, ia menggunakan JNI (Java Native Interface) untuk mengirim pesan string ke lapisan aplikasi.
   * Fungsi C++ memanggil method Java secara asinkron untuk mengirim log teks status terkini.
 * UI Handler (Kotlin - Tampilan):
   Di sisi aplikasi NxMod, sebuah listener menerima pesan tersebut dan menampilkannya pada komponen teks gulir (scrolling text view).
   * Visualisasi: Teks diformat dengan warna (Hijau untuk sukses, Merah untuk error) agar memberikan efek visual "hacking" yang sedang berjalan secara realtime.
   
5. Konfigurasi Build System (Environment Integrasi)
Berdasarkan referensi cara build dan environment AndroidIDE:
 * Penyatuan Source Code:
   Library BNM dan Hash-Library diintegrasikan langsung ke dalam folder jni proyek NxM. Metode ini disebut Source Inclusion, di mana file .cpp dan .h library tersebut didaftarkan langsung ke dalam variabel LOCAL_SRC_FILES di Android.mk.
 * Kompilasi Tunggal:
   Dengan konfigurasi ini, saat menekan tombol "Build" di AndroidIDE, seluruh komponen (NxM Core + BNM + Hash-Lib + Main.cpp dan lain-lainnya) akan dikompilasi menjadi satu file apk yang didalamnya terdapat file tunggal (libNxMod.so) yang siap diinjeksi.
   
 ***
   
Ringkasan Topologi NxMod Baru:
 * Start: Game berjalan -> libNxMod.so dimuat.
 * Visual: UI Log muncul: [SYSTEM] Initializing NxMod Core....
 * Scan: BNM bekerja -> Log update: [SCAN] Searching Method: get_Gold... FOUND at 0x7A1....
 * Hook: Cpp bekerja -> Log update: [HOOK] Intercepting Network Traffic... SUCCESS.
 * Action: User mengubah nilai -> Hash-Lib bekerja -> Log update: [NET] Re-calculating Checksum... Validated & Sent.
 
 ***
 
 Catatan : 
 
1. Izin Memori (Android 15):
Karena Anda menggunakan CompileSdk 35, Android sekarang sangat ketat terhadap dynamic code loading. Pastikan dalam AndroidManifest.xml Anda tidak melupakan atribut android:extractNativeLibs="true" jika Anda melakukan injeksi manual, atau pastikan library .so berada di lokasi yang tepat agar tidak terkena blokir kebijakan Read-only executable memory.

2. Kesesuaian NDK 28 & CMake 4.1.1:
Pada NDK versi terbaru, LLVM/Clang adalah compiler default. Pastikan pada file Android.mk atau CMakeLists.txt, Anda menambahkan flag -fvisibility=hidden untuk menyembunyikan simbol fungsi Anda agar tidak mudah di-reverse engineering oleh sistem anti-cheat game.

3. Optimalisasi JNI Bridge:
Untuk Visualisasi Log Realtime, pastikan pemanggilan JNI dari C++ ke Java/Kotlin dilakukan di Background Thread. Jika C++ memanggil UI thread secara langsung (Blocking call), game akan mengalami frame drop atau stuttering. Gunakan AttachCurrentThread dari env JNI dengan hati-hati.

4. XXHash vs Hash-Library:
XXHash: Sangat baik jika Anda melakukan bypass pada fungsi Update() atau FixedUpdate() yang berjalan 60 kali per detik.
Hash-Library (SHA-256): Gunakan hanya saat paket data akan dikirim (saat send() terpanggil), karena enkripsi SHA cukup memakan resource CPU jika dijalankan setiap frame.