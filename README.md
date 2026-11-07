# FlowSnap

Android-first MVP for turning a user-consented sequence of app screenshots into a UX flow.

## Milestone 1 — implemented

- Kotlin + Jetpack Compose Android project (min SDK 26, target SDK 35)
- Home UI: create and open named flows
- Room database for local flow metadata
- Diagram destination screen and explicit capture consent entry point
- MediaProjection foreground service with a draggable overlay capture control and review step
- PDF export through the Android Storage Access Framework

## Run it

1. Open this folder in Android Studio Ladybug or newer.
2. Let Gradle sync and install Android SDK Platform 35 if Android Studio asks.
3. Select an Android 8.0+ device/emulator and press **Run**.

The Gradle wrapper is checked in. To build from the command line, use a Java 17 JDK and run `gradlew.bat :app:assembleDebug` on Windows.

## Planned milestones

2. Editor: pan/zoom Compose canvas, draggable nodes, manually-marked transition anchors, and arrows.
3. Delivery: diagram PNG export through `Bitmap`/`Canvas`, Android share sheet, tests, and a release-ready privacy/data-deletion pass.

## Important capture rules

- The user must approve capture for **each** projection session.
- A consent result and its `MediaProjection` can create only one virtual display on Android 14+; do not cache/reuse either.
- The service must start after approval, become foreground immediately with type `mediaProjection`, and release projection/display/image resources from `MediaProjection.Callback.onStop()`.
- Android 14+ allows the system picker to offer a single app window or the full display; FlowSnap should support both, including size-change callbacks.

See the [Android MediaProjection guide](https://developer.android.com/media/grow/media-projection) and [foreground-service type requirements](https://developer.android.com/about/versions/14/changes/fgs-types-required).
