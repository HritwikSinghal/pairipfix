---
name: pairip-hook-coverage
description: pairip license bypass coverage -- which LicenseClient/LicenseActivity/validator names each app ships, hook misses in LSPosed log, new pairip versions
status: active
---

# pairip hook coverage

## Current state
All hooks target exact pairip class/method names. Verified working on one app (Play Store disabled, license check started without a pairip ContentProvider). Unknown: which of the hooks actually matched there, and how other pairip versions name things.

## Next actions
1. On each newly tested app, record the LSPosed `[PairIPFix] <Group>: x/y` lines and any `failed to hook` lines here.
2. When a name is missing, add the new variant beside the old one, gated on `classExists` (see `CLAUDE.md` Invariants).

## Decisions
- 2026-10-23: Hook `handleError`/`startErrorDialogActivity` with `hookAllDoNothing` because their parameter types were not observed; safe only while they stay `void`.

## Findings

### Failure path observed on a real app (2026-10-23)
Play Store disabled, module not installed, `LicenseActivity` disabled with a component manager. logcat:

```
E/LicenseClient: com.pairip.licensecheck.LicenseCheckException: Could not bind with the
                 licensing service: com.android.vending.licensing.ILicensingService
    at com.pairip.licensecheck.LicenseClient.bindToLicensingService(LicenseClient.java:359)
    at com.pairip.licensecheck.LicenseClient.lambda$retryOrThrow$1(LicenseClient.java:563)
E/AndroidRuntime: FATAL EXCEPTION: main
  android.content.ActivityNotFoundException: Unable to find explicit activity class
  {<pkg>/com.pairip.licensecheck.LicenseActivity}
    at com.pairip.licensecheck.LicenseClient.startErrorDialogActivity(LicenseClient.java:676)
    at com.pairip.licensecheck.LicenseClient.lambda$handleError$0(LicenseClient.java:659)
```

- With `LicenseActivity` re-enabled the same app shows "Something went wrong -- Check that Google Play is enabled..." and CLOSE kills the app (`closeApp`/`exitApp`).
- The app registered no `com.pairip.*` ContentProvider (`adb shell dumpsys package <pkg>` provider list), so the usual "disable the pairip provider" trick has nothing to disable.
- `adb shell dumpsys package <pkg> | sed -n '/disabledComponents:/,/enabledComponents:/p'` shows what a component manager disabled.

### Useful commands
- Reproduce and read a crash: `adb logcat -c`, launch the app, then `adb logcat -d | grep -iE "pairip|LicenseClient|AndroidRuntime"`.
- Confirm the module ran: LSPosed log lines prefixed `[PairIPFix]`; `Total: N/M hooks applied for <pkg>`.
