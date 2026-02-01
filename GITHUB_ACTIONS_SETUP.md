# 🔧 GitHub Actions Setup Guide

**Project:** Habit Tracker  
**Workflow:** `.github/workflows/build-apk.yml`  
**Purpose:** Automated CI/CD for building Android APKs

---

## 📋 SUMMARY

The GitHub Actions workflow automatically builds your Android app whenever you push to `main` or `master` branch. It:

1. ✅ Runs unit tests
2. ✅ Builds Debug APK (always available)
3. ✅ Builds Release APK (if secrets configured)

---

## 🚀 QUICK START (Debug Build Only)

**No setup required!** The workflow will automatically build debug APKs on every push.

**To Download Debug APK:**
1. Push code to GitHub
2. Go to **Actions** tab in your repository
3. Click on the latest workflow run
4. Download `app-debug` artifact

---

## 🔐 SETUP FOR RELEASE BUILDS

To build signed release APKs, you need to configure GitHub Secrets.

### Step 1: Generate Keystore (One-Time)

If you don't have a keystore yet:

```bash
keytool -genkey -v -keystore release.keystore -keyalg RSA -keysize 2048 -validity 10000 -alias key0
```

**Important:** Save the password! You'll need it for the next step.

---

### Step 2: Encode Keystore to Base64

**On Linux/Mac:**
```bash
base64 -w 0 release.keystore > release.keystore.base64
```

**On Windows (PowerShell):**
```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("release.keystore")) | Out-File release.keystore.base64
```

**On Windows (Git Bash):**
```bash
base64 -w 0 release.keystore > release.keystore.base64
```

---

### Step 3: Add Secrets to GitHub

1. Go to your repository on GitHub
2. Click **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret**
4. Add the following secrets:

| Secret Name | Value | Description |
|-------------|-------|-------------|
| `KEYSTORE_BASE64` | Content of `release.keystore.base64` | Base64-encoded keystore file |
| `KEYSTORE_PASSWORD` | Your keystore password | Password from step 1 |
| `KEY_ALIAS` | `key0` (or your alias) | Key alias from step 1 |
| `KEY_PASSWORD` | Your key password | Usually same as keystore password |

---

### Step 4: Push to Trigger Build

After configuring secrets, push to `main` or `master`:

```bash
git add .
git commit -m "Enable release builds"
git push origin main
```

The workflow will now build both debug and release APKs!

---

## 📥 DOWNLOADING APKs

### From GitHub Website:

1. Go to **Actions** tab
2. Click on latest workflow run
3. Scroll to **Artifacts** section
4. Click to download:
   - `app-debug` (always available)
   - `app-release` (if secrets configured)

### From GitHub CLI (gh):

```bash
# List artifacts
gh run view --log

# Download artifacts
gh run download
```

---

## 🔍 TROUBLESHOOTING

### Issue: "Unrecognized named-value: 'secrets'"

**Solution:** This should be fixed now. The workflow uses a check step to verify secrets exist before using them.

### Issue: Release APK not building

**Possible Causes:**
1. Secrets not configured → Check Settings → Secrets → Actions
2. Keystore password incorrect → Verify all secrets
3. Keystore not base64-encoded properly → Re-encode using correct command

### Issue: Build fails with " Keystore was tampered with"

**Solution:** The keystore password or key password is incorrect. Double-check your secrets.

---

## 📊 WORKFLOW DETAILS

### What the Workflow Does:

```yaml
1. Checkout code
2. Set up JDK 17
3. Run unit tests
4. Build Debug APK
5. Upload Debug APK
6. Check if secrets available
7. If yes:
   - Decode keystore
   - Build Release APK
   - Upload Release APK
```

### Build Types:

| Type | When Built | Signing | Artifacts |
|------|-----------|---------|-----------|
| **Debug** | Always | Debug key | `app-debug.zip` |
| **Release** | Secrets configured | Release keystore | `app-release.zip` |

---

## 🔐 SECURITY BEST PRACTICES

### ✅ Do:
- Use strong passwords for keystore
- Never commit `.keystore` or `.jks` files
- Never commit passwords
- Use GitHub Secrets (not variables)
- Rotate keystore periodically

### ❌ Don't:
- Hardcode passwords in workflow
- Commit keystore to repository
- Share secrets publicly
- Use the same password for multiple projects

---

## 📝 ADDITIONAL NOTES

### Keystore Location

The workflow stores the decoded keystore at `app/release.keystore`. This matches the build configuration:

```kotlin
// app/build.gradle.kts
signingConfigs {
    create("release") {
        val keystorePath = System.getenv("KEYSTORE_PATH")
        if (keystorePath != null) {
            storeFile = file(keystorePath)
            // ...
        }
    }
}
```

### Artifact Retention

APKs are kept for 30 days. Adjust `retention-days` in the workflow if needed.

---

## 🎯 TESTING LOCALLY

Before pushing, test locally:

```bash
# Test debug build
./gradlew assembleDebug

# Test release build (with keystore)
export KEYSTORE_PATH=app/release.keystore
export KEYSTORE_PASSWORD=your_password
export KEY_ALIAS=key0
export KEY_PASSWORD=your_key_password
./gradlew assembleRelease
```

---

## 📞 SUPPORT

If you encounter issues:

1. Check **Actions** tab for detailed logs
2. Verify all secrets are set correctly
3. Ensure keystore is valid (can build locally)
4. Check Gradle version compatibility

---

## 🔄 WORKFLOW CUSTOMIZATION

### Change Branches:

```yaml
on:
  push:
    branches: [ "main", "develop", "release/*" ]
```

### Run Manually:

The workflow includes `workflow_dispatch`, so you can trigger it manually:
1. Go to **Actions** tab
2. Select **Build Android App** workflow
3. Click **Run workflow**

### Disable Tests:

Comment out or remove the test step:

```yaml
# - name: Run Unit Tests
#   run: ./gradlew testDebugUnitTest --continue
```

---

## ✅ CHECKLIST

Before expecting release builds to work:

- [ ] Keystore generated
- [ ] Keystore encoded to base64
- [ ] `KEYSTORE_BASE64` secret added
- [ ] `KEYSTORE_PASSWORD` secret added
- [ ] `KEY_ALIAS` secret added
- [ ] `KEY_PASSWORD` secret added
- [ ] Tested local build with secrets
- [ ] Pushed to main/master branch

---

**Last Updated:** February 1, 2026  
**Workflow Version:** 2.0 (Fixed secrets syntax)
