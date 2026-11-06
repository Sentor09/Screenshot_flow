# FlowSnap

Android-first MVP for turning a user-consented sequence of app screenshots into a UX flow.

## Milestone 1 — implemented

- Kotlin + Jetpack Compose Android project (min SDK 26, target SDK 35)
- Home UI: create and open named flows
- Room database for local flow metadata
- Diagram destination screen and explicit capture consent entry point
- Android 14+ `mediaProjection` foreground-service declaration and notification host

## Run it

1. Open this folder in Android Studio Ladybug or newer.
2. Let Gradle sync and install Android SDK Platform 35 if Android Studio asks.
3. Select an Android 8.0+ device/emulator and press **Run**.

There is no checked-in Gradle wrapper because this empty workspace did not contain a Gradle installation to generate it. Android Studio can sync the project directly; after it does, use **Gradle > wrapper** or `gradle wrapper` to add a wrapper for command-line builds.

## Planned milestones

2. `MediaProjection` session: consume the one-time result inside `CaptureService`, run an `ImageReader` virtual display, render a user-enabled overlay control, and persist screenshots plus normalized tap markers.
3. Editor: Room tables for nodes/transitions; a pan/zoom Compose canvas, draggable nodes, manually-marked transition anchors, and arrows.
4. Delivery: local PNG canvas export through `Bitmap`/`Canvas`, PDF export through `PdfDocument`, Android share sheet, tests, and a release-ready privacy/data-deletion pass.

## Important capture rules

- The user must approve capture for **each** projection session.
- A consent result and its `MediaProjection` can create only one virtual display on Android 14+; do not cache/reuse either.
- The service must start after approval, become foreground immediately with type `mediaProjection`, and release projection/display/image resources from `MediaProjection.Callback.onStop()`.
- Android 14+ allows the system picker to offer a single app window or the full display; FlowSnap should support both, including size-change callbacks.

See the [Android MediaProjection guide](https://developer.android.com/media/grow/media-projection) and [foreground-service type requirements](https://developer.android.com/about/versions/14/changes/fgs-types-required).
