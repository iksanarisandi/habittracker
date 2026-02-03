# Android CI/CD Quick Reference

## Critical Build Rules

### ViewBinding
- ❌ NEVER: `binding.findViewById()`
- ❌ NEVER: `ItemChartBarBinding.bind(binding.bar1)`
- ✅ ALWAYS: `binding.bar1.tvDay` (auto-generated for includes)
- ✅ IDs match exactly: `@+id/tvName` → `binding.tvName`

### Resources
- ❌ NEVER: Use `R.string.*` without verifying it exists
- ✅ ALWAYS: Check `strings.xml` before using
- ✅ FALLBACK: `@android:drawable/sym_def_app_icon` for missing icons

### Coroutines
- ❌ NEVER: `scope.cancel()` on CoroutineScope
- ✅ ALWAYS: Store `Job` separately: `job.cancel()`

### Shell Scripts
- ❌ NEVER: `[ "$-" = *x* ]` in POSIX sh
- ✅ ALWAYS: `case "$-" in *x*) set -x ;; esac`

## Common Error → Fix Mapping

| Error | Fix |
|-------|-----|
| `resource mipmap/ic_launcher not found` | Use `@android:drawable/sym_def_app_icon` |
| `Unresolved reference: findViewById` | Use ViewBinding: `binding.viewId` |
| `Type mismatch: ItemChartBarBinding but View` | Don't use `.bind()` on auto-generated bindings |
| `Unresolved reference: cancel` | Store `Job` separately, cancel job not scope |
| `./gradlew: 202: [: PRD.txt: unexpected operator` | Use `case` statement, not `[ ]` with glob |
| `Unresolved reference: tvHabitName` | Check XML ID, use exact match: `binding.tvName` |
| `Unresolved reference: habit_name_hint` | Add to `strings.xml` or use hardcoded string |

## Version Compatibility

```
Kotlin  1.9.24  →  KSP 1.9.24-1.0.20
AGP      8.5.2   →  Gradle ≥8.7, Java 17+
Gradle   8.7     →  Java 17-22
```

## Pre-Push Commands

```bash
# 1. Update dependencies (if needed)
./gradlew wrapper --gradle-version=8.7

# 2. Clean build
./gradlew clean

# 3. Build debug APK
./gradlew assembleDebug

# 4. Check compilation
./gradlew compileDebugKotlin

# 5. Run tests (if exist)
./gradlew testDebugUnitTest

# 6. Verify APK exists
ls -lh app/build/outputs/apk/debug/*.apk
```

## GitHub Actions Quick Commands

```bash
# List recent runs
gh run list --limit 10

# View specific run
gh run view <run-id>

# View failed logs only
gh run view <run-id> --log-failed

# Watch current run
gh run watch
```

## File Locations

| What | Where |
|------|-------|
| Icon refs | `app/src/main/AndroidManifest.xml` |
| ViewBinding code | `app/src/main/java/**/*.kt` |
| String resources | `app/src/main/res/values/strings.xml` |
| View IDs | `app/src/main/res/layout/*.xml` |
| Gradle config | `build.gradle.kts`, `settings.gradle.kts` |
| CI workflow | `.github/workflows/build-apk.yml` |
| Shell script | `gradlew` (line ~202) |

## 5-Step Diagnostic Process

1. **Check logs**: `gh run view <run-id> --log-failed`
2. **Identify pattern**: Match error to table above
3. **Apply fix**: Use documented solution
4. **Test locally**: `./gradlew assembleDebug`
5. **Commit & push**: `git commit -m "fix: [description]"`

## Emergency Fixes

### Build failing immediately
→ Check `gradlew` syntax: `sh -n gradlew`

### Resource linking failed
→ Check `AndroidManifest.xml` icon refs

### Compilation errors
→ Check ViewBinding IDs match XML IDs

### ViewBinding errors
→ Remove any `.bind()` calls on included layouts

---

**Full Guide**: [.factory/ANDROID_CI_BEST_PRACTICES.md](ANDROID_CI_BEST_PRACTICES.md)
**Troubleshooting Skill**: [.factory/skills/android-ci-troubleshooting.md](skills/android-ci-troubleshooting.md)
