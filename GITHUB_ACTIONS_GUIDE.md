# 🚀 CI/CD Setup Guide (GitHub Actions)

This project is configured with a robust GitHub Actions workflow (`.github/workflows/build-apk.yml`) that automatically builds your Android app.

## 📋 Features
-   **Automated Testing**: Runs unit tests on every push/PR.
-   **Debug Builds**: Always generates a debug APK for testing.
-   **Release Builds**: Automatically generates a signed release APK **if** secrets are configured.

---

## 🔐 Setting Up Signed Release Builds

To enable automatic release builds, you need to configure **GitHub Secrets**.

### 1. Generate a Keystore (If you haven't already)
Run this command in your terminal (or use Android Studio):
```bash
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
```
*Remember the password and alias you used!*

### 2. Encode Keystore to Base64
GitHub Secrets cannot store binary files directly. You need to encode your `.jks` file to Base64 string.

**Mac/Linux:**
```bash
base64 -i my-release-key.jks > keystore_base64.txt
```

**Windows (PowerShell):**
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("my-release-key.jks")) | Out-File -Encoding ascii keystore_base64.txt
```

### 3. Add Secrets to GitHub
Go to your GitHub Repository -> **Settings** -> **Secrets and variables** -> **Actions** -> **New repository secret**.

Add the following 4 secrets:

| Secret Name | Value |
|-------------|-------|
| `KEYSTORE_BASE64` | The content of `keystore_base64.txt` (one long string) |
| `KEYSTORE_PASSWORD` | The password for the keystore file |
| `KEY_ALIAS` | The alias name you used (e.g., `my-key-alias`) |
| `KEY_PASSWORD` | The password for the key (usually same as keystore password) |

---

## 🏃‍♂️ How to Run
1.  **Push** code to `main` or open a **Pull Request**.
2.  Go to the **Actions** tab in GitHub.
3.  You will see the workflow running.
4.  Once finished, check **Artifacts** to download `app-debug.apk` (and `app-release.apk` if configured).
