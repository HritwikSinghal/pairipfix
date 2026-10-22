# pairipfix -- Todo

## Notes & Conventions
- Durable architecture, invariants, build and release steps live in the root `CLAUDE.md`; this file only tracks work.
- Public fork: never record device details, local paths, key locations, personal accounts, or the names of apps a maintainer uses. Describe a tested app by how its pairip check behaves instead.
- Releases are cut by running the `Build` workflow manually on `main` with a `version` input; never publish a debug-signed APK.

## Phase 2: Follow-ups
- [ ] Decide on signing-key rotation -- the release certificate subject contains the maintainer's GitHub handle; rotating means a new key + secrets, a new release, and one uninstall/reinstall per device
- [ ] README -- replace the upstream-era text with download (GitHub Releases), LSPosed setup, and the component-disable trap
- [ ] pairip-hook-coverage -- active, verified on one app only -- `claude/workstreams/pairip-hook-coverage.md`
- [ ] Record the per-group `applied/attempted` counts from the LSPosed log on the verified app, to see which hooks actually matched -- `claude/workstreams/pairip-hook-coverage.md`

## Done

### Phase 1: Working module + release pipeline (2026-10-23)
- [x] Relax the VMRunner-only detection guard (any of VMRunner / LicenseClient / LicenseClientV3)
- [x] Hook the error path (`handleError`, `startErrorDialogActivity`) and finish `LicenseActivity` in `onCreate`
- [x] Per-hook counting and exception logging; guard `FULL_CHECK_OK`; skip non-first packages
- [x] Fix mangled namespace/applicationId; drop unused AndroidX deps and placeholder scope
- [x] GitHub Actions build/release pipeline with signing, wrapper validation, monotonic versionCode
- [x] Release v1.3 (versionCode 20) and verify on a device with the Play Store disabled
- [x] Root `CLAUDE.md` for future agents

<!--
An item that belongs to a workstream carries slug, status or blocker, and pointer.
A bare title is the single biggest reason the right file never gets opened.
This file is injected in full at session start -- keep it skimmable.
-->
