# Target Arsitektur (Sesuai device kamu)
APP_ABI := arm64-v8a

# Minimum API Level
# NDK r28 minimal support android-21.
# Sebaiknya diset manual agar tidak warning.
APP_PLATFORM := android-21

# Gunakan static agar tidak perlu dependency luar saat inject
APP_STL := c++_static

# Optimasi Release
APP_OPTIM := release

# WAJIB FALSE untuk build di HP/Termux agar library statis terbaca sempurna
APP_THIN_ARCHIVE := false

# Fitur keamanan (opsional, tapi bagus)
APP_CPPFLAGS += -fexceptions -frtti
