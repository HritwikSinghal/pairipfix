# pairipfix -- Progress
> Last updated: 2026-10-23 | Session: 1

## Current state
Objective: keep an LSPosed module that lets pairip-protected apps run without the Play Store, with signed releases built by CI.

v1.3 (versionCode 20) is released on GitHub, signed with the CI release key, and verified working on one device with the Play Store disabled, on an app whose license check starts without a pairip ContentProvider. Push/PR builds and the manual release path both ran green. Nothing is blocked; the open items are follow-ups.

| Phase | Status | Progress |
|-------|--------|----------|
| Phase 1: Working module + release pipeline | Done | 7/7 |
| Phase 2: Follow-ups | Pending | 0/4 |

Workstreams in flight:
- pairip-hook-coverage -- verified on one app; coverage of other pairip versions unknown -- `claude/workstreams/pairip-hook-coverage.md`

## Next actions
1. Decide whether to rotate the signing key (see todo); cheapest before more installs exist.
2. Rewrite the README for download + LSPosed setup.
3. Collect LSPosed hook counts from more pairip apps to find renamed methods.

## Decisions (durable)
- 2026-10-23: Stop the license check inside the app (hooks) rather than disabling pairip components. Disabling components cannot work for apps whose check does not start from a ContentProvider, and disabling `LicenseActivity` crashes them.
- 2026-10-23: Version = `workflow_dispatch` input; versionCode = commit count floored at the previous release's code + 1 (hidden marker in release notes). Rejected: committing a version bump back to `main` (as some upstream pipelines do) -- more moving parts for a one-module repo.
- 2026-10-23: Dedicated release key for this module, held as GitHub secrets; releases refuse to run without it. Rejected: reusing another project's key (its secrets cannot be read back) and debug signing (breaks updates).
- 2026-10-23: `LicenseActivity` is finished in `onCreate`, not just left with no-op close methods -- the latter leaves a dialog with a dead CLOSE button.

## Session log
- 2026-10-23 (S1): Diagnosed a pairip app crashing after `LicenseActivity` was disabled (error path launching a disabled activity). Fixed the module's detection guard and coverage gaps, added the CI release pipeline and signing, released v1.3, verified on device, wrote `CLAUDE.md`.

<!--
Not in this file:
  - the phase checklist          -> claude/todo.md
  - per-topic narrative, findings, commands, dead ends
                                 -> claude/workstreams/<slug>.md
Only the first 4,000 chars are injected at session start, so the top must stand alone.
-->
