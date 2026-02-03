# Habit Tracker (MVP)

Aplikasi Android sederhana untuk melacak kebiasaan harian (Habit Tracker) dengan arsitektur MVP dan dukungan Offline-First.

## Fitur
- **Manajemen Kebiasaan**: Tambah, Edit, Hapus kebiasaan.
- **Frekuensi**: Harian atau Mingguan.
- **Streak Tracking**: Menghitung hari/minggu berturut-turut.
- **Reminder**: Notifikasi lokal (offline).
- **Offline-First**: Menggunakan Room Database.
- **Ringan**: Tanpa login, tanpa internet.

## Arsitektur
- **MVP (Model-View-Presenter)**
- **Language**: Kotlin
- **Database**: Room
- **Background Work**: WorkManager

## Cara Build
1.  **Prasyarat**:
    - Android Studio Iguana atau lebih baru.
    - JDK 17.

2.  **Setup Gradle Wrapper** (PENTING):
    Karena file `gradlew` tidak disertakan dalam pembuatan awal, silakan jalankan perintah berikut di root project menggunakan terminal Android Studio atau instalasi Gradle lokal Anda:
    ```bash
    gradle wrapper
    ```
    Kemudian commit file `gradlew`, `gradlew.bat`, dan `gradle/wrapper/*` yang terbentuk.

3.  **Build APK**:
    ```bash
    ./gradlew assembleRelease
    ```

## CI/CD (GitHub Actions)
Workflow build otomatis sudah dikonfigurasi di `.github/workflows/build-apk.yml`.
Untuk mengaktifkan Signing (APK ter-sign), tambahkan Secrets berikut di Repository Settings > Secrets and variables > Actions:
- `KEYSTORE_BASE64`: Konten file keystore (.jks) yang di-encode base64.
- `KEYSTORE_PASSWORD`: Password keystore.
- `KEY_ALIAS`: Alias key.
- `KEY_PASSWORD`: Password key.

### Troubleshooting CI Build Failures
Jika build gagal di GitHub Actions, referensi panduan lengkap untuk best practices dan troubleshooting:
- **[Android CI/CD Best Practices Guide](.factory/ANDROID_CI_BEST_PRACTICES.md)** - Panduan lengkap dan aturan untuk build APK yang berhasil
- **[Android CI Troubleshooting Skill](.factory/skills/android-ci-troubleshooting.md)** - Skill khusus untuk diagnosa dan perbaikan masalah build Android

Panduan ini mencakup:
- ViewBinding best practices dan common pitfalls
- Resource management dan missing resource errors
- Kotlin compilation issues
- Gradle configuration dan version compatibility
- Pre-push checklist untuk mencegah build failures

## Pre-Push Checklist
Sebelum push kode yang memicu CI build:
- [ ] Test lokal: `./gradlew assembleDebug` sukses
- [ ] Verify semua resource `R.*` sudah ada
- [ ] Tidak ada penggunaan `findViewById()` saat ViewBinding enabled
- [ ] String resources baru sudah ditambahkan ke `strings.xml`
- [ ] Notification icons menggunakan `android.R.drawable` atau custom drawable
- [ ] ViewBinding IDs match dengan XML IDs
- [ ] Job references disimpan terpisah dari CoroutineScope untuk cancellation
