# pairipfix

An LSPosed (Xposed) module that disables Google's "pairip" Play licensing check inside apps,
so a pairip-protected app runs when it was sideloaded or when the Play Store is disabled. It
works purely by hooking pairip's Java classes; it does not touch pairip's native VM
(`libpairipcore.so`) or its anti-Frida/anti-debug checks.

## Layout

- `app/src/main/assets/xposed_init` -- names the entry class LSPosed loads
  (`io.github.ahmedmani.pairipfix.mainHook`). Renaming or moving `mainHook` requires updating it.
- `mainHook.java` -- runs per loaded package: skips non-first packages, detects pairip by marker
  classes, applies every `BaseHook`, logs per-group and total `applied/attempted` counts.
- `BaseHook.java` -- hook helpers. Every helper call is one counted attempt; failures log the
  exception. Add hooks only through these helpers so the counts stay honest.
- `hooks/` -- one class per concern:
  - `LicenseClientBypass` -- stops the check (`initializeLicenseCheck`, `performLocalInstallerCheck`,
    `licenseCheckState = FULL_CHECK_OK`) and its error path (`handleError`, `startErrorDialogActivity`).
  - `LicenseActivityBypass` -- the "Something went wrong / Get this app from Play" dialog:
    finishes it in `onCreate` and no-ops its close/exit methods.
  - `LicenseResponseBypass` -- response/signature validation (`licensecheck` and `licensecheck3`).
  - `SignatureBypass` -- `com.pairip.SignatureCheck`, for re-signed APKs.
- `.github/workflows/build.yml` -- the only CI: build, artifact, release.

## How pairip's license check fails (what the hooks target)

On launch pairip's `LicenseClient` binds Play's licensing service
(`com.android.vending.licensing.ILicensingService`). When that bind fails -- Play disabled or
absent -- the path observed in a real crash trace is:

```
LicenseClient.retryOrThrow -> bindToLicensingService (LicenseCheckException)
  -> handleError -> startErrorDialogActivity -> startActivity(LicenseActivity)
```

`LicenseActivity` is the dialog; its CLOSE button calls `closeApp`/`exitApp`, which kill the app.

Trap: users often "fix" pairip by disabling its components (e.g. with App Manager). Disabling a
pairip ContentProvider works for apps that start the check from that provider. Not every app has
one; disabling `LicenseActivity` instead does not stop the check, and the error path then crashes
with `ActivityNotFoundException`. That is why the module must stop the check itself.

## Invariants

- `isMinifyEnabled = false` for release: LSPosed loads `mainHook` by the name in `xposed_init`.
- `de.robv.android.xposed:api:82` stays `compileOnly`; bundling it breaks the module.
- `hookAllDoNothing` replaces every overload with a null-returning stub: use it only on `void`
  methods whose parameter types are unknown. Use `hookDoNothing`/`hookReturnConstant` with exact
  parameter types otherwise.
- Hooks match pairip class and method names exactly. New pairip versions may rename them; a miss
  shows as a `failed to hook` line, not a crash. Add a new variant beside the old, gated on
  `classExists`, rather than replacing it -- older apps still ship the old names.
- Java 8 source level; log lines start with `[PairIPFix]`.
- The module has no UI and no runtime dependencies. Keep it that way.

## Build

```
./gradlew assembleRelease -PversionCode=<int> -PversionName=<name>
```

Output: `app/build/outputs/apk/release/app-release.apk`. Needs `platforms;android-34` and
`build-tools;35.0.0` with licences accepted. Without `ReleaseKey.properties` at the repo root the
release build falls back to the debug key (fine locally; never publish such an APK).

There are no unit tests: the hooks only do anything inside a hooked app process. Verify on a
device instead (below).

## CI and releases (`.github/workflows/build.yml`)

- Push / PR to `main`: builds and uploads `pairipfix-<ref>-<sha7>.apk` as an Actions artifact.
- Release: run the workflow manually on `main` with input `version` (e.g. `1.4`). It tags
  `v<version>` at the built commit and publishes `pairipfix-v<version>.apk` to a GitHub Release.
- Signing secrets: `SIGNING_KEY` (base64 keystore), `KEY_STORE_PASSWORD`, `ALIAS`, `KEY_PASSWORD`.
  A release aborts without them. Never rotate the key casually: an APK signed with a different key
  cannot update an existing install (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`).
- `versionCode` = commit count, floored at the previous release's code + 1. Each release body ends
  with a hidden `<!-- versionCode=N -->` marker that the next build reads; do not strip it when
  editing release notes.
- Secrets and `workflow_dispatch` inputs reach shell steps only via `env:`, never inline `${{ }}`.

## Verifying on a device

1. Install the APK, enable the module in LSPosed Manager, add the target app to its scope
   (the module ships no recommended scope), force-stop the app, relaunch.
2. LSPosed log: expect `[PairIPFix] Total: N/M hooks applied for <package>`. Per-group lines and
   `failed to hook ...: <exception>` lines show exactly which names did not match.
3. `adb logcat` filtered on `LicenseClient|pairip|AndroidRuntime` shows whether the check or its
   error path still runs.

## Long-Running Project

This project uses session-persistent tracking in `claude/`. At the start of every session:
1. Read `claude/todo.md` and `claude/progress.md` silently for a full catch-up -- do not ask the user to re-explain anything.
2. Open a `claude/workstreams/<slug>.md` only when the task matches its manifest line. Do not bulk-read them.
3. Do NOT automatically continue working -- wait for the user to indicate they want to proceed.
4. After each completed task, update the tracker immediately: mark `[x]` in `claude/todo.md`, recount the Status Summary and rewrite `## Current state` in `claude/progress.md`, and record any topic detail in that topic's workstream file.

The tracker is public: never record device details, local paths, key locations, personal accounts, or the names of apps a maintainer uses.
