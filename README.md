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
