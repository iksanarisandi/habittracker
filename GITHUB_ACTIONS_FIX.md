# GitHub Actions Error Fix - Quick Guide

## 🐛 Original Error

```
Invalid workflow file: .github/workflows/build-apk.yml
(Line: 42, Col: 11): Unrecognized named-value: 'secrets'. Located at position 1 within expression: secrets.KEYSTORE_BASE64 != ''
```

---

## 🔴 Root Cause

GitHub Actions doesn't allow using `secrets.*` directly in conditional expressions like:
```yaml
if: secrets.KEYSTORE_BASE64 != ''  # ❌ THIS CAUSES ERROR
```

---

## ✅ Solution Applied

Changed to use a **check step** that verifies secrets exist first:

```yaml
# Step 1: Check if secrets are available
- name: Check if secrets are available
  id: check_secrets
  run: |
    if [ -n "${{ secrets.KEYSTORE_PASSWORD }}" ] && [ -n "${{ secrets.KEY_PASSWORD }}" ]; then
      echo "available=true" >> $GITHUB_OUTPUT
    else
      echo "available=false" >> $GITHUB_OUTPUT
    fi

# Step 2: Use the output in conditions
- name: Decode Keystore
  if: steps.check_secrets.outputs.available == 'true'  # ✅ THIS WORKS
  run: |
    echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 --decode > app/release.keystore
```

---

## 📋 What Changed

### Before (❌ Broken):
```yaml
- name: Decode Keystore
  if: secrets.KEYSTORE_BASE64 != ''  # Invalid syntax
  run: |
    echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 --decode > app/release.keystore
```

### After (✅ Fixed):
```yaml
- name: Check if secrets are available
  id: check_secrets
  run: |
    if [ -n "${{ secrets.KEYSTORE_PASSWORD }}" ] && [ -n "${{ secrets.KEY_PASSWORD }}" ]; then
      echo "available=true" >> $GITHUB_OUTPUT
    else
      echo "available=false" >> $GITHUB_OUTPUT
    fi

- name: Decode Keystore
  if: steps.check_secrets.outputs.available == 'true'  # Valid syntax
  run: |
    echo "${{ secrets.KEYSTORE_BASE64 }}" | base64 --decode > app/release.keystore
```

---

## 🎯 Why This Works

1. **Check step** runs a shell command to check if secrets exist
2. **Output variable** (`available`) is set to `true` or `false`
3. **Conditional steps** use `steps.check_secrets.outputs.available` instead of accessing `secrets.*` directly
4. This is the **recommended pattern** by GitHub Actions

---

## 🚀 Next Steps

1. **Verify the fix is applied** - Check `.github/workflows/build-apk.yml`
2. **Commit and push** - The workflow should now work
3. **Setup secrets** (optional) - Follow `GITHUB_ACTIONS_SETUP.md` for release builds

---

## 📚 Additional Resources

- [GitHub Actions: Contexts](https://docs.github.com/en/actions/learn-github-actions/contexts)
- [GitHub Actions: Variables](https://docs.github.com/en/actions/learn-github-actions/variables)
- [Encrypted secrets](https://docs.github.com/en/actions/security-guides/encrypted-secrets)

---

**Fixed:** February 1, 2026  
**Status:** ✅ Ready to use
