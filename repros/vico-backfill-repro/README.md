# Vico Backfill Repro

This is a minimal Android-only repro for the Compose `CartesianChartHost` backfill transition issue.

What it does:

- renders a cached trailing page first
- waits `3000ms`
- replaces that model with a wider 4-page history through the same `CartesianChartModelProducer`
- keeps the viewport pinned to the end with `Scroll.Absolute.End`
- fixes the visible domain to one page with `Zoom.x(27.0)`
- replays the transition every `8000ms`

On `com.patrykandpatrick.vico:compose:3.1.0`, this reproduces the bad one-frame transition where the chart can briefly render before snapped auto-scroll has been applied.

## Run

From this directory:

```bash
./gradlew :app:installDebug
adb shell am start -n com.example.vicobackfillrepro/.MainActivity
```

## Toggle The Vico Version

The sample currently uses:

```kotlin
implementation("com.patrykandpatrick.vico:compose:3.1.0")
```

in `app/build.gradle.kts`.
