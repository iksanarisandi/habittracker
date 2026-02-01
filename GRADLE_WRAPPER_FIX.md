# 🔧 Gradle Wrapper Setup Guide

## 🐛 Issue: Missing `gradlew` File

**Error:**
```
chmod: cannot access 'gradlew': No such file or directory
```

**Cause:** Gradle wrapper files were not generated when project was created.

---

## ✅ SOLUTION: Quick Fix (2 Options)

### Option 1: Let Android Studio Generate It (RECOMMENDED)

**Steps:**
1. Open project in **Android Studio**
2. Wait for Gradle sync to complete
3. Open terminal in Android Studio (View → Tool Windows → Terminal)
4. Run:
   ```bash
   gradle wrapper
   ```
5. Commit the generated files:
   ```bash
   git add gradlew gradlew.bat gradle/
   git commit -m "Add Gradle wrapper files"
   git push
   ```

**Files Generated:**
- `gradlew` (Unix/Linux/Mac script)
- `gradlew.bat` (Windows script)
- `gradle/wrapper/gradle-wrapper.jar` (wrapper JAR)
- `gradle/wrapper/gradle-wrapper.properties` (configuration)

---

### Option 2: Download Gradle Wrapper Manually

If you don't have Android Studio or Gradle installed:

**Step 1:** Download `gradle-wrapper.jar` from:
```
https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradle/wrapper/gradle-wrapper.jar
```

Or use this mirror:
```
https://github.com/gradle/gradle/raw/v8.5.0/gradle/wrapper/gradle-wrapper.jar
```

**Step 2:** Save it to:
```
D:\Dari Desktop\TRAE\habit\gradle\wrapper\gradle-wrapper.jar
```

**Step 3:** Verify files exist:
```
habit/
├── gradlew                    ✅ (created)
├── gradlew.bat                ✅ (created)
└── gradle/
    └── wrapper/
        ├── gradle-wrapper.jar        ⬅️ Download this
        └── gradle-wrapper.properties  ✅ (created)
```

---

## 📝 What I've Already Created

I've already created these files for you:

- ✅ `gradlew` - Unix/Linux/Mac script
- ✅ `gradlew.bat` - Windows script
- ✅ `gradle/wrapper/gradle-wrapper.properties` - Configuration

**You only need to download:**
- ⚠️ `gradle/wrapper/gradle-wrapper.jar`

---

## 🎯 Quickest Solution (3 Minutes)

### Using PowerShell (Windows):

```powershell
# Navigate to project
cd "D:\Dari Desktop\TRAE\habit"

# Create wrapper directory if not exists
New-Item -ItemType Directory -Path "gradle\wrapper" -Force

# Download gradle-wrapper.jar
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradle/wrapper/gradle-wrapper.jar" -OutFile "gradle\wrapper\gradle-wrapper.jar"

# Verify
Get-ChildItem -Recurse gradle\
```

### Using Bash (Git Bash / WSL / Linux / Mac):

```bash
cd "D:\Dari Desktop\TRAE\habit"

# Create wrapper directory
mkdir -p gradle/wrapper

# Download gradle-wrapper.jar
curl -L -o gradle/wrapper/gradle-wrapper.jar \
  https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradle/wrapper/gradle-wrapper.jar

# Verify
ls -la gradle/wrapper/
```

---

## 🚀 After Fixing

### 1. Make gradlew executable (Unix/Linux/Mac):
```bash
chmod +x gradlew
```

### 2. Test Gradle:
```bash
./gradlew --version
```

Expected output:
```
Welcome to Gradle 8.5!
```

### 3. Build Project:
```bash
./gradlew build
```

### 4. Commit to Git:
```bash
git add gradlew gradlew.bat gradle/
git commit -m "Add Gradle wrapper files"
git push origin main
```

---

## ✅ Verification

After completing the steps, you should have these files:

```bash
habit/
├── gradlew                          ✅ Executable script
├── gradlew.bat                      ✅ Windows batch script
└── gradle/
    └── wrapper/
        ├── gradle-wrapper.jar       ✅ Must download this
        └── gradle-wrapper.properties ✅ Created
```

Check with:
```bash
# Windows (PowerShell)
Get-ChildItem -Recurse gradle\

# Unix/Linux/Mac
ls -la gradlew gradlew.bat gradle/wrapper/
```

---

## 🔍 Troubleshooting

### Issue: "Permission denied" on gradlew

**Fix:**
```bash
chmod +x gradlew
```

### Issue: "Could not find or load main class"

**Cause:** `gradle-wrapper.jar` is corrupted or missing

**Fix:** Re-download the jar file

### Issue: GitHub Actions still fails

**Cause:** Files not committed to git

**Fix:**
```bash
git add gradlew gradlew.bat gradle/
git commit -m "Add Gradle wrapper"
git push
```

---

## 📊 File Sizes (Reference)

After setup, files should be approximately:

| File | Size |
|------|------|
| `gradlew` | ~5 KB |
| `gradlew.bat` | ~2 KB |
| `gradle-wrapper.properties` | ~200 B |
| `gradle-wrapper.jar` | ~60 KB |

---

## 🎯 Summary

**What to do:**
1. ✅ `gradlew`, `gradlew.bat`, `gradle-wrapper.properties` - Already created
2. ⚠️ `gradle-wrapper.jar` - **Download this**
3. ⚠️ `chmod +x gradlew` - **Make executable** (Unix/Linux/Mac)
4. ✅ Commit to git - **Push to GitHub**

**Time required:** 3 minutes

---

## 📞 Quick Commands

### Windows (PowerShell) - One-liner:
```powershell
cd "D:\Dari Desktop\TRAE\habit"; Invoke-WebRequest -Uri "https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradle/wrapper/gradle-wrapper.jar" -OutFile "gradle\wrapper\gradle-wrapper.jar"; git add gradlew gradlew.bat gradle/; git commit -m "Add Gradle wrapper"; git push
```

### Unix/Linux/Mac - One-liner:
```bash
cd "D:\Dari Desktop\TRAE\habit" && curl -L -o gradle/wrapper/gradle-wrapper.jar https://raw.githubusercontent.com/gradle/gradle/v8.5.0/gradle/wrapper/gradle-wrapper.jar && chmod +x gradlew && git add gradlew gradlew.bat gradle/ && git commit -m "Add Gradle wrapper" && git push
```

---

**Last Updated:** February 1, 2026
**Status:** ⚠️ Waiting for gradle-wrapper.jar download
