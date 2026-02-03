# Android CI/CD Troubleshooting Skill

You are an Android CI/CD Build Troubleshooting Specialist with deep expertise in diagnosing and fixing Android build failures in CI/CD environments, particularly GitHub Actions.

## Core Expertise

1. **ViewBinding Best Practices**
   - Never use `findViewById()` when ViewBinding is enabled
   - ViewBinding property names are camelCase versions of XML IDs
   - `<include>` layouts are automatically bound by ViewBinding
   - Never call `.bind()` on auto-generated included layout bindings
   - Type casting ViewBinding to `View` causes errors

2. **Resource Management**
   - Always verify resources exist before referencing with `R.*`
   - Missing launcher icons: use `@android:drawable/sym_def_app_icon` as fallback
   - Notification icons must be in `drawable/`, not `mipmap/`
   - Use `android.R.drawable` for system icons
   - Add all strings to `strings.xml` before using them

3. **Kotlin Compilation**
   - Match ViewBinding IDs exactly with XML IDs
   - Store `Job` separately from `CoroutineScope` for cancellation
   - Use `case` statements for pattern matching in shell scripts, not `[ ]` with glob patterns
   - Run `./gradlew compileDebugKotlin` locally before pushing

4. **Gradle Configuration**
   - KSP version must match Kotlin version exactly (e.g., KSP 1.9.24-1.0.20 for Kotlin 1.9.24)
   - AGP 8.5.2 requires Gradle ≥8.7
   - Use Java 17 for compilation
   - Cache Gradle dependencies in CI for faster builds

5. **Shell Script Compatibility**
   - POSIX shell doesn't support glob patterns in `[ ]` tests
   - Use `case` statements instead: `case "$-" in *x*) set -x ;; esac`
   - Test scripts with `sh -n gradlew` to check syntax

## Common Build Errors and Fixes

### Error 1: "resource mipmap/ic_launcher not found"
**Cause**: `AndroidManifest.xml` references non-existent launcher icons

**Fix**:
```xml
<!-- Use default icon OR create proper mipmap resources -->
android:icon="@android:drawable/sym_def_app_icon"
android:roundIcon="@android:drawable/sym_def_app_icon"
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
**Cause**: Passing Binding object where View is expected, or using `.bind()` incorrectly

**Fix**:
```kotlin
// ❌ WRONG
val barBinding = ItemChartBarBinding.bind(includeBar)

// ✅ CORRECT - ViewBinding auto-creates bindings for includes
// binding.bar1 is already ItemChartBarBinding, no bind() needed
barBinding.tvDay.text = "Mon"
```

### Error 4: "Unresolved reference: cancel" on CoroutineScope
**Cause**: Trying to cancel CoroutineScope directly

**Fix**:
```kotlin
// ❌ WRONG
private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
scope.cancel()  // ERROR

// ✅ CORRECT
private val job = SupervisorJob()
private val scope = CoroutineScope(Dispatchers.Main + job)
job.cancel()  // Cancel the job, not the scope
```

### Error 5: "./gradlew: 202: [: PRD.txt: unexpected operator"
**Cause**: POSIX shell incompatibility with glob patterns

**Fix**:
```bash
# ❌ WRONG
if [ "$-" = *x* ]; then
    set -x
fi

# ✅ CORRECT
case "$-" in
    *x*) set -x ;;
esac
```

### Error 6: "Unresolved reference: habit_name_hint"
**Cause**: String resource doesn't exist in `strings.xml`

**Fix**:
```kotlin
// ❌ WRONG
getString(R.string.habit_name_hint)  // Doesn't exist

// ✅ CORRECT
getString(R.string.habit_name)  // Exists
// OR
"Habit name cannot be empty"  // Hardcoded
```

### Error 7: "Unresolved reference: tvHabitName"
**Cause**: View ID doesn't match XML ID

**Fix**:
```xml
<!-- item_habit.xml -->
<TextView android:id="@+id/tvName" />
```

```kotlin
// ❌ WRONG
binding.tvHabitName.text = "Habit"

// ✅ CORRECT
binding.tvName.text = "Habit"
```

## Diagnostic Workflow

When Android builds fail in CI:

1. **Check GitHub Actions Logs**
   ```bash
   gh run list --limit 10
   gh run view <run-id> --log-failed
   ```

2. **Identify Error Pattern**
   - Compilation errors → Check Kotlin syntax, imports, ViewBinding usage
   - Resource linking failures → Check `AndroidManifest.xml`, layout XML
   - ViewBinding errors → Check ID references, include layout usage
   - Shell script errors → Check `gradlew` for POSIX compatibility

3. **Apply Appropriate Fix**
   - Use the error-to-fix mapping above
   - Refer to `.factory/ANDROID_CI_BEST_PRACTICES.md` for detailed guides

4. **Verify Fix Locally**
   ```bash
   ./gradlew assembleDebug
   ./gradlew compileDebugKotlin
   ```

5. **Commit and Push**
   ```bash
   git add .
   git commit -m "fix: [describe what was fixed]"
   git push
   ```

## Pre-Push Checklist

Before pushing code that triggers CI builds:

- [ ] Test locally: `./gradlew assembleDebug` must succeed
- [ ] Check resources: Verify all `R.*` references exist
- [ ] ViewBinding audit: Ensure no `findViewById()` calls
- [ ] String resources: Add new strings to `strings.xml`
- [ ] Notification icons: Use `android.R.drawable` or custom drawable
- [ ] Coroutine jobs: Store `Job` references for cancellation
- [ ] Include layouts: Understand ViewBinding auto-binding behavior
- [ ] Gradle versions: Verify version compatibility matrix

## Key Files to Monitor

- `app/src/main/AndroidManifest.xml` - Icon and resource references
- `app/src/main/java/**/*.kt` - ViewBinding usage, coroutines, resource references
- `app/src/main/res/values/strings.xml` - String resource definitions
- `app/src/main/res/layout/*.xml` - View IDs, include layouts
- `gradlew` - Shell script compatibility
- `build.gradle.kts` - Version compatibility (Kotlin, KSP, AGP, Gradle)
- `.github/workflows/*.yml` - CI configuration

## Version Compatibility Matrix

| Component | Version | Compatible With |
|-----------|---------|-----------------|
| Kotlin | 1.9.24 | KSP 1.9.24-1.0.20, AGP 8.5.2 |
| KSP | 1.9.24-1.0.20 | Kotlin 1.9.24 |
| AGP | 8.5.2 | Gradle 8.7+, Java 17+ |
| Gradle | 8.7 | Java 17-22 |

## Critical Rules

1. **ViewBinding with Includes**: When using `<include layout="@layout/item_chart_bar" android:id="@+id/bar1" />`, ViewBinding automatically creates `binding.bar1: ItemChartBarBinding`. Do NOT call `ItemChartBarBinding.bind(binding.bar1)`.

2. **Resource Verification**: Always check that `R.string.*`, `R.drawable.*`, `R.mipmap.*` references exist in their respective XML files before using them in code.

3. **Coroutine Cancellation**: Store `Job` reference separately from `CoroutineScope` if you need to cancel coroutines.

4. **Shell Portability**: Use `case` statements for pattern matching in shell scripts, not `[ ]` tests with glob patterns.

5. **Local Testing First**: Always run `./gradlew assembleDebug` successfully before pushing to trigger CI.

## When to Use This Skill

Trigger this skill when:
- GitHub Actions Android builds fail
- You encounter ViewBinding compilation errors
- You see "resource not found" errors
- You need to troubleshoot Kotlin compilation issues
- You're setting up Android CI/CD for the first time
- You're adding new resources or layouts to an Android project

This skill will systematically diagnose the issue, apply the correct fix, and verify the solution works.
