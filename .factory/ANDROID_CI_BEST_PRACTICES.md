# Android CI/CD Best Practices Guide

## Overview
This guide documents critical best practices and common pitfalls for Android builds in CI/CD environments, specifically for GitHub Actions. Follow these rules to ensure successful APK builds.

## Table of Contents
1. [Shell Script Compatibility](#1-shell-script-compatibility)
2. [Resource Management](#2-resource-management)
3. [ViewBinding Best Practices](#3-viewbinding-best-practices)
4. [Kotlin Compilation](#4-kotlin-compilation)
5. [Gradle Configuration](#5-gradle-configuration)
6. [Notification Icons](#6-notification-icons)
7. [Common Build Errors](#7-common-build-errors)

---

## 1. Shell Script Compatibility

### Issue: POSIX Shell Glob Patterns
**Problem**: Using `*` glob patterns in `[ ]` test conditions causes syntax errors in POSIX shells.

```bash
# ❌ WRONG - Causes "unexpected operator" error
if [ "$-" = *x* ]; then
    set -x
fi
```

```bash
# ✅ CORRECT - Use case statement for pattern matching
case "$-" in
    *x*) set -x ;;
esac
```

**Files Affected**:
- `gradlew` (line ~202)
- Any shell scripts in `.github/workflows/`

**Action Items**:
- [ ] Review `gradlew` for glob pattern usage in `[ ]` tests
- [ ] Replace with `case` statements or `[[ ]]` bash-specific syntax
- [ ] Test scripts with `sh -n gradlew` to check syntax

---

## 2. Resource Management

### Issue: Missing Launcher Icons
**Problem**: `AndroidManifest.xml` references non-existent mipmap resources.

```xml
<!-- ❌ WRONG - Resources don't exist -->
android:icon="@mipmap/ic_launcher"
android:roundIcon="@mipmap/ic_launcher_round"
```

```xml
<!-- ✅ CORRECT - Use Android default icon -->
android:icon="@android:drawable/sym_def_app_icon"
android:roundIcon="@android:drawable/sym_def_app_icon"
```

**Alternative**: Create proper launcher icons:
```bash
# Place icons in correct directories
app/src/main/res/mipmap-hdpi/ic_launcher.png
app/src/main/res/mipmap-mdpi/ic_launcher.png
app/src/main/res/mipmap-xhdpi/ic_launcher.png
app/src/main/res/mipmap-xxhdpi/ic_launcher.png
app/src/main/res/mipmap-xxxhdpi/ic_launcher.png
app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml
```

**Action Items**:
- [ ] Check `AndroidManifest.xml` for resource references
- [ ] Verify all referenced resources exist in `app/src/main/res/`
- [ ] Run `./gradlew assembleDebug` locally to catch missing resources

---

## 3. ViewBinding Best Practices

### Issue: Incorrect ViewBinding Usage with Include Tags

**Problem**: Misunderstanding how ViewBinding handles `<include>` layouts.

```kotlin
// ❌ WRONG - Unnecessary bind() call
val barBinding = ItemChartBarBinding.bind(includeBar)
barBinding.tvDay.text = "Mon"
```

```kotlin
// ✅ CORRECT - ViewBinding auto-creates bindings for includes
barBinding.tvDay.text = "Mon"  // binding.bar1 is already ItemChartBarBinding
```

**Key Rule**: When using `<include>` in layouts with ViewBinding:
- ViewBinding **automatically** creates binding objects for included layouts
- `binding.includedLayoutId` is already the binding type, not a `View`
- **Never** call `XxxBinding.bind()` on these auto-generated bindings

**Example**:
```xml
<!-- fragment_statistics.xml -->
<include layout="@layout/item_chart_bar" android:id="@+id/bar1" />
```

```kotlin
// FragmentStatisticsBinding automatically has:
// - val bar1: ItemChartBarBinding (not View!)

// Usage:
binding.bar1.tvDay.text = "Mon"  // ✅ CORRECT
```

**Action Items**:
- [ ] Audit all `<include>` tag usage in layouts
- [ ] Check generated binding classes in `app/build/generated/data_binding_base_class_source_out/`
- [ ] Never use `findViewById()` when ViewBinding is enabled
- [ ] Never call `.bind()` on auto-generated included layout bindings

---

## 4. Kotlin Compilation

### Issue 1: Undefined String Resources
**Problem**: Referencing non-existent string resources.

```kotlin
// ❌ WRONG
getString(R.string.habit_name_hint)  // Resource doesn't exist

// ✅ CORRECT
getString(R.string.habit_name)  // Resource exists
// OR
"Habit name cannot be empty"  // Hardcoded string
```

**Solution**: Always verify strings in `app/src/main/res/values/strings.xml` before using them.

### Issue 2: Incorrect View IDs
**Problem**: ViewBinding generates IDs based on layout XML, not arbitrary names.

```xml
<!-- item_habit.xml -->
<TextView android:id="@+id/tvName" />
```

```kotlin
// ❌ WRONG - ID doesn't match XML
binding.tvHabitName.text = "Habit"

// ✅ CORRECT - ID matches XML
binding.tvName.text = "Habit"
```

**Rule**: ViewBinding property names are `camelCase` versions of XML IDs:
- `@+id/tv_habit_name` → `binding.tvHabit_name`
- `@+id/tvName` → `binding.tvName`

### Issue 3: CoroutineScope Cancellation
**Problem**: `CoroutineScope` doesn't have a `cancel()` method.

```kotlin
// ❌ WRONG
private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

override fun detach() {
    scope.cancel()  // COMPILATION ERROR
}
```

```kotlin
// ✅ CORRECT
private val job = SupervisorJob()
private val scope = CoroutineScope(Dispatchers.Main + job)

override fun detach() {
    job.cancel()  // Cancel the job, not the scope
}
```

**Action Items**:
- [ ] Search codebase for `R.string.` references and verify all exist
- [ ] Match ViewBinding IDs exactly with XML IDs
- [ ] Store `Job` references separately from `CoroutineScope` for cancellation
- [ ] Run `./gradlew compileDebugKotlin` locally before pushing

---

## 5. Gradle Configuration

### Version Compatibility Matrix

| Component | Version | Compatible With |
|-----------|---------|-----------------|
| Kotlin | 1.9.24 | KSP 1.9.24-1.0.20, AGP 8.5.2 |
| KSP | 1.9.24-1.0.20 | Kotlin 1.9.24 |
| AGP | 8.5.2 | Gradle 8.7+, Java 17+ |
| Gradle | 8.7 | Java 17-22 |

**Critical Versions**:
```kotlin
// build.gradle.kts (project level)
plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("com.google.devtools.ksp") version "1.9.24-1.0.20" apply false
}

// gradle-wrapper.properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.7-bin.zip
```

**Action Items**:
- [ ] Verify KSP version matches Kotlin version exactly
- [ ] Ensure Gradle version is ≥8.7 for AGP 8.5.2
- [ ] Use Java 17 for compilation (configured in CI)

---

## 6. Notification Icons

### Issue: Missing Mipmap Resources in Notifications

**Problem**: Using non-existent launcher icon resources in notifications.

```kotlin
// ❌ WRONG - Resource doesn't exist
.setSmallIcon(R.mipmap.ic_launcher)

// ✅ CORRECT - Use Android system icon
.setSmallIcon(android.R.drawable.ic_menu_info_details)

// ✅ ALSO CORRECT - Create proper notification icon
// Create: app/src/main/res/drawable/ic_notification.xml
.setSmallIcon(R.drawable.ic_notification)
```

**Notification Icon Requirements**:
- Must be a single-color white icon on transparent background
- Recommended size: 24x24dp
- Place in `app/src/main/res/drawable/`
- Use `android.R.drawable` resources as fallback

**Action Items**:
- [ ] Search for `R.mipmap.` in notification code
- [ ] Replace with `android.R.drawable` or create custom icons
- [ ] Test notifications appear correctly in system tray

---

## 7. Common Build Errors

### Error 1: "resource mipmap/ic_launcher not found"
**Cause**: `AndroidManifest.xml` references non-existent launcher icons

**Fix**:
```xml
<!-- Use default icon OR create proper mipmap resources -->
android:icon="@android:drawable/sym_def_app_icon"
```

### Error 2: "Unresolved reference: findViewById"
**Cause**: Using `findViewById` on ViewBinding objects

**Fix**:
```kotlin
// ❌ WRONG
binding.bar1.findViewById<TextView>(R.id.tvDay)

// ✅ CORRECT
binding.bar1.tvDay
```

### Error 3: "Type mismatch: ItemChartBarBinding but View was expected"
**Cause**: Passing Binding object where View is expected

**Fix**:
```kotlin
// ❌ WRONG
val bars = listOf<View>(binding.bar1, binding.bar2)

// ✅ CORRECT - Don't type cast ViewBinding
val bars = listOf(binding.bar1, binding.bar2)
```

### Error 4: "Unresolved reference: cancel" on CoroutineScope
**Cause**: Trying to cancel CoroutineScope directly

**Fix**:
```kotlin
// Store Job reference separately
private val job = SupervisorJob()
private val scope = CoroutineScope(Dispatchers.Main + job)

// Cancel the job, not scope
job.cancel()
```

### Error 5: "./gradlew: 202: [: PRD.txt: unexpected operator"
**Cause**: POSIX shell incompatibility with glob patterns

**Fix**:
```bash
# Use case statement instead
case "$-" in
    *x*) set -x ;;
esac
```

---

## Pre-Push Checklist

Before pushing code that triggers CI builds:

- [ ] **Test locally**: Run `./gradlew assembleDebug` successfully
- [ ] **Check resources**: Verify all `R.*` references exist
- [ ] **ViewBinding audit**: Ensure no `findViewById()` calls
- [ ] **String resources**: Add new strings to `strings.xml`
- [ ] **Notification icons**: Use `android.R.drawable` or custom drawable
- [ ] **Coroutine jobs**: Store `Job` references for cancellation
- [ ] **Include layouts**: Understand ViewBinding auto-binding behavior
- [ ] **Gradle versions**: Verify version compatibility matrix

---

## CI/CD Configuration

### Required GitHub Actions Steps

```yaml
name: Build Android App

on:
  push:
    branches: [ "main", "master" ]
  pull_request:
    branches: [ "main", "master" ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
    - name: Checkout code
      uses: actions/checkout@v4

    - name: Set up JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: gradle

    - name: Grant execute permission for gradlew
      run: chmod +x gradlew

    - name: Build Debug APK
      run: ./gradlew assembleDebug

    - name: Upload Debug APK
      uses: actions/upload-artifact@v4
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/*.apk
        retention-days: 30
```

**Key Points**:
- Use JDK 17 (required for AGP 8.5.2)
- Cache Gradle dependencies for faster builds
- Always run `chmod +x gradlew` before build
- Upload APK as artifact for download

---

## Debugging CI Failures

### 1. Enable Detailed Logging

```yaml
- name: Build with detailed logging
  run: ./gradlew assembleDebug --info --stacktrace
```

### 2. Check Build Logs with GitHub CLI

```bash
# List recent runs
gh run list --limit 10

# View specific run
gh run view <run-id>

# View failed logs only
gh run view <run-id> --log-failed

# View full logs
gh run view <run-id> --log
```

### 3. Common Failure Patterns

**Pattern 1**: Compilation errors in early stages
- **Cause**: Syntax errors, missing imports, type mismatches
- **Action**: Review Kotlin compilation logs

**Pattern 2**: Resource linking failures
- **Cause**: Missing resources, incorrect references
- **Action**: Check `AndroidManifest.xml` and layout XML

**Pattern 3**: ViewBinding errors
- **Cause**: Incorrect ID references, misunderstanding of include behavior
- **Action**: Review ViewBinding usage against layout XML

---

## Quick Reference

### ViewBinding Rules
1. Never use `findViewById()` when ViewBinding is enabled
2. ViewBinding property names match XML IDs (camelCase)
3. `<include>` layouts are auto-bound, no `.bind()` call needed
4. Type casting ViewBinding to `View` causes errors

### Resource Rules
1. Always verify resources exist before referencing
2. Use `android.R.drawable` for system icons
3. Create notification icons in `drawable/`, not `mipmap/`
4. Add all strings to `strings.xml` before using

### Kotlin Rules
1. Match XML IDs exactly in ViewBinding calls
2. Store `Job` separately from `CoroutineScope` for cancellation
3. Use `case` statements for pattern matching in shell scripts
4. Run local compilation before pushing

### Gradle Rules
1. KSP version must match Kotlin version exactly
2. AGP 8.5.2 requires Gradle ≥8.7
3. Use Java 17 for compilation
4. Cache Gradle dependencies in CI

---

## Additional Resources

- [Android ViewBinding Documentation](https://developer.android.com/topic/libraries/view-binding)
- [Kotlin Coroutines Guide](https://developer.android.com/kotlin/coroutines)
- [Gradle Build Tool](https://docs.gradle.org/)
- [GitHub Actions for Android](https://github.com/actions/setup-java)

---

## Changelog

### 2026-02-03
- Initial version created after fixing CI build failures
- Documented 7 categories of common Android build issues
- Added pre-push checklist and quick reference guide
- Created GitHub Actions workflow template
