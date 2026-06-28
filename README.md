<!-- Banner -->
<p align="center">
  <img src="docs/banner.png" alt="Build By God" width="100%" />
</p>

<h1 align="center">
  <img src="docs/icon.png" alt="Build By God icon" width="92" height="92" align="center" />
  &nbsp;Build By God
</h1>

<p align="center">
  <i>“Build By God” — powered by hemanth.</i><br/>
  A native Android <b>gym workout planner</b> with a full glossy dark UI.
</p>

<p align="center">
  <img alt="Platform" src="https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white" />
  <img alt="Language" src="https://img.shields.io/badge/Kotlin-2.0-7F52FF?logo=kotlin&logoColor=white" />
  <img alt="UI" src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white" />
  <img alt="Min SDK" src="https://img.shields.io/badge/minSdk-26-blue" />
  <img alt="Target SDK" src="https://img.shields.io/badge/targetSdk-35-blue" />
  <img alt="License" src="https://img.shields.io/badge/License-Proprietary%20%C2%B7%20All%20Rights%20Reserved-red" />
</p>

---

Plan your week, get reminders when it's time to train, follow guided workouts (warm-ups, exercises,
stretches), and browse a **fully-offline** library of **1,100+ exercises** across **22 muscle groups** —
including dedicated **warm-ups, stretches, and equipment-free home workouts** for every group — each
with how-tos and a bundled looping demo animation.

> **100% offline.** No account, no login, no internet — every exercise, instruction, and demo clip
> ships inside the app. Nothing is ever fetched from YouTube or the web.

## ⬇️ Download

Ready-to-install builds live in this repo:

- **Signed release:** [`apk/release/BuildByGod-v0.3.0.apk`](apk/release/BuildByGod-v0.3.0.apk)
- **Debug build:** [`apk/devel/BuildByGod-devel.apk`](apk/devel/BuildByGod-devel.apk)

Copy the APK to an Android phone (Android 8.0 / API 26+), tap it, and allow installation from this source.

## ✨ Features

- **Glossy theme with full control** — light / dark / follow-system modes, six selectable accent
  color combinations (Aurora, Ocean, Sunset, Forest, Grape, Crimson), and a glass-intensity slider
  to dial the frosted look from translucent to solid. All applied live across the app.
- **Credential-free profile** — name, goal, units, reminder preferences (stored via DataStore).
- **Body metrics & nutrition** — store height, weight, age, sex, and activity level; the app computes
  BMR + TDEE (Mifflin-St Jeor), a goal-adjusted daily calorie target, protein/carb/fat macros, and a
  water target. Profile also shows workout count, streak, total minutes, and estimated calories burned.
- **Responsive & inset-aware** — edge-to-edge UI that works with both gesture and 3-button navigation,
  and centers content on tablets / large screens.
- **Weekly plan** — assign a workout to each day of the week, set a time, toggle reminders, mark rest days.
- **Time-based notifications** — exact weekly alarms remind you when it's time to train; tapping a
  reminder deep-links straight into that day's workout. Reminders survive reboots.
- **Home dashboard** — today's workout, a week strip, streaks, and totals at a glance.
- **Day detail** — split into Warm-up / Exercises / Stretches; add or remove moves per section.
- **1,100+ exercise library, fully offline** — browse by muscle target across **22 groups** (Abs,
  Obliques, Chest, Shoulders, Traps, Biceps, Triceps, Forearms, Palmar Fascia, Lats, Upper/Lower
  Back, Neck, Glutes, Hip Flexors, Adductors, Abductors, Quads, Hamstrings, Calves, IT Band,
  Plantar Fascia), search, favorite, and open rich detail pages (step-by-step how-to + pro tips).
- **Warm-ups, stretches & home workouts per group** — each muscle group includes its own dynamic
  warm-ups, cool-down stretches, and equipment-free (bodyweight / band / household-object) home
  workouts, plus universal full-body warm-up and stretch routines.
- **Bundled animated demos** — exercises ship with a looping animated-WebP demo that plays offline
  (rendered via Coil), with a glossy placeholder where no clip exists. No YouTube, no streaming,
  no network calls.
- **Guided session mode** — step through a day's workout with set targets and built-in timers for
  timed moves, then log it to your history.
- **Progress** — streak, weekly count, an 8-week activity heatmap, and recent session history.

## 🧱 Tech stack

- Kotlin + **Jetpack Compose** + Material 3 (custom glossy dark color scheme)
- MVVM + Repository pattern
- **Room** (exercises, plan, sessions) + **DataStore** (profile & settings)
- **Hilt** for dependency injection
- **Navigation-Compose**
- **AlarmManager** + boot receiver for reminders
- **Coil** for offline animated-WebP demo playback (bundled in `assets/`)
- Min SDK 26, Target SDK 35

## 📁 Project structure

```
app/src/main/java/com/buildbygod/
  BuildByGodApp.kt          # Hilt app + notification channel
  MainActivity.kt           # Compose host + deep-link handling
  data/                     # Room entities, DAOs, DB, seed data, DataStore, repositories
  domain/model/             # Enums (MuscleGroup, ExerciseType, Goal, Units, ...)
  di/                       # Hilt modules
  notifications/            # ReminderScheduler, ReminderReceiver, BootReceiver
  ui/
    theme/                  # Colors, type, glossy components (GlassCard, GradientButton, ProgressRing)
    components/             # Background, top bar, exercise row, demo media
    navigation/             # Routes + bottom bar
    onboarding/ home/ plan/ daydetail/ library/ exercise/ session/ progress/ profile/

apk/
  devel/                    # debug APKs
  release/                  # signed release APKs (when built)
docs/                       # README banner + icon
```

## 🛠️ Building

The Gradle wrapper is checked in, so you can build without Android Studio.

**With Android Studio** (Koala or newer): open the `BuildByGod` folder, let it sync, then Run on an
emulator or device (API 26+).

**From the command line** (needs JDK 17 + Android SDK):

```bash
# point Gradle at your SDK
echo "sdk.dir=/path/to/Android/Sdk" > local.properties

# build the debug APK -> app/build/outputs/apk/debug/app-debug.apk
./gradlew :app:assembleDebug
```

## 🎬 Offline demo clips

The whole catalogue is bundled in the app — no network is ever used:

- `app/src/main/assets/exercises.json` — all **1,100+ exercises** (name, type, muscle group, equipment,
  step-by-step instructions, tips, and the demo clip filename).
- `app/src/main/assets/clips/*.webp` — looping **animated WebP** demo clips (~20 MB total),
  played inline via Coil. On Android 9+ they animate; on 8.x they show the first frame.

On first launch `DatabaseSeeder` reads the JSON, seeds Room, and builds a balanced 7-day starter plan
from the seeded exercises. Demo frames are derived from the public-domain free-exercise-db (Unlicense).

## 🔔 Notifications

On API 33+ the app requests `POST_NOTIFICATIONS` (from the Profile screen). On API 31+ it will
fall back to inexact alarms if exact-alarm permission isn't granted. All reminders are rescheduled
after a reboot via `BootReceiver`.

## 📝 Notes

- 100% offline — no backend, no analytics, no network permission needed for content.
- First launch seeds the full 1,100+ exercise library and a sensible 7-day starter plan, which you can
  fully customize.

## 🙏 Credits

- Exercise data and demo animations are derived from the
  [free-exercise-db](https://github.com/yuhonas/free-exercise-db) (released under the **Unlicense** /
  public domain). All content is bundled and used offline.

## ⚖️ License

**Proprietary — All Rights Reserved.** Copyright © 2026 Hemanth Selam.

This project is **not** open source. No permission is granted to use, copy, modify, or distribute
the code or assets without the owner's prior written consent. See [LICENSE](LICENSE) for the full
terms. The repository is public for viewing/portfolio purposes only.

---

<p align="center">
  <b>Build By God</b> · powered by <a href="https://github.com/SelamHemanth">hemanth</a><br/>
  <sub>© 2026 Hemanth Selam · All Rights Reserved</sub>
</p>
