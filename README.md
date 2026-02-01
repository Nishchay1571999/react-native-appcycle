# react-native-appcycle

A React Native library for **app lifecycle awareness** (foreground/background), a **global overlay** (BottomSheet-style UI), and **Android assistant integration**. Use it to show a React-powered overlay from the background, from a Quick Settings tile, from the accessibility button, or (on supported devices) from long-press home as the default digital assistant.

---

## Features

- **App lifecycle**: Subscribe to `onForeground` / `onBackground` and optional heartbeat (`onIsLive`) from a foreground service.
- **Overlay UI**: A modal BottomSheet rendered in your app’s React tree, with full access to your app API (fetch, DB, etc.).
- **Overlay triggers (Android)**:
  - In-app: `useOverlay().open()` or `openOverlay()`.
  - Background: start a foreground runtime service; the service can bring the app to front and show the overlay (e.g. from a notification action).
  - Quick Settings tile: add “Open overlay” tile; tap to open overlay.
  - Accessibility: enable “Appcycle overlay trigger” in Settings → Accessibility; use the accessibility button or shortcut to open the overlay.
  - Default assistant (where supported): set the app as default digital assistant; long-press home opens your overlay (optional; requires extra app setup).
- **Overlay-only mode**: When launched from the assistant, show only the overlay (transparent background) and optionally “Open full app” to switch to the main activity.
- **iOS**: Lifecycle events (`onForeground` / `onBackground`) are supported; overlay and Android-specific APIs are no-ops or return safely.

---

## Requirements

- **React Native** 0.72+ (tested with 0.83)
- **React** 18+
- **Android**: minSdk 24, compileSdk 36
- **iOS**: min version as required by your React Native version
- **Node** >= 20 (for the example app)

---

## Installation

```bash
npm install react-native-appcycle
# or
yarn add react-native-appcycle
```

**Peer dependencies**: `react` and `react-native` (any compatible version). The library is built with React Native 0.83 and React 19; it should work with React Native 0.72+ and React 18+.

**Expo**: This library uses native Android/iOS code and is not compatible with Expo Go. Use [development builds](https://docs.expo.dev/develop/development-builds/introduction/) (or bare workflow) if you use Expo.

### iOS

```bash
cd ios && pod install && cd ..
```

### Android

No extra steps for basic overlay and lifecycle. The library ships an `AndroidManifest` that declares:

- A foreground **runtime service** (for background heartbeat and “trigger overlay from background”).
- An **accessibility service** (optional; user enables in Settings to open overlay via accessibility button/shortcut).
- A **Quick Settings tile** (user adds the tile to open overlay).

Your app must:

1. **Handle the overlay intent** so the library (or your own code) can bring the app to front and show the overlay. In your **main/launcher Activity**, add an intent-filter for `com.appcycle.SHOW_OVERLAY` (see [Android setup](#android-setup) below).
2. **Start the runtime from the foreground** on Android 12+ if you use the background “trigger overlay” flow; foreground service start from background is restricted.

---

## Android setup

### 1. Main Activity: handle `SHOW_OVERLAY`

Your launcher Activity (e.g. `MainActivity`) must handle the overlay intent so that when the runtime service, Quick Settings tile, or accessibility service starts the app with “open overlay”, your app receives the intent and can show the overlay.

**In `AndroidManifest.xml`** (inside the `<activity>` for your main component):

```xml
<intent-filter>
    <action android:name="android.intent.action.MAIN" />
    <category android:name="android.intent.category.LAUNCHER" />
</intent-filter>
<!-- Required: so the runtime service / tile / accessibility can start the app with openOverlay=true -->
<intent-filter>
    <action android:name="com.appcycle.SHOW_OVERLAY" />
    <category android:name="android.intent.category.DEFAULT" />
</intent-filter>
```

**In your Activity class** (e.g. Kotlin), forward new intents so the JS layer can read `openOverlay` via `getAndClearLaunchExtra`:

```kotlin
override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    intent?.let { setIntent(it) }
}
```

### 2. (Optional) Overlay-only Activity for assistant

If you want “overlay-only” mode when the app is invoked from the assistant (only the BottomSheet, transparent background), add a separate Activity (e.g. `OverlayOnlyActivity`) that:

- Uses a transparent theme.
- Registers the same React component name as your main app (same bundle).
- Is **not** in the launcher; you start it from your VoiceInteractionSession (or similar) with extras `openOverlay=true` and `openOverlayOnly=true`.

The library does not start this Activity for you; the example app implements it for reference.

### 3. (Optional) Default digital assistant

To appear in “Settings → Voice input” and handle long-press home as the default assistant, you need to implement and declare:

- `VoiceInteractionService`
- `VoiceInteractionSessionService`
- `RecognitionService`

and start either your main Activity with `openOverlay` + `openOverlayOnly` or your overlay-only Activity. The library provides `openDefaultAssistantSettings()` to open the system screen where the user selects the default assistant; it does not implement the voice/assistant stack itself. See the example app for a minimal setup.

---

## Usage

### 1. Wrap your app with `OverlayProvider`

Your root component (or the subtree that needs overlay and lifecycle) should be wrapped in `OverlayProvider`:

```tsx
import { OverlayProvider, OverlayContainer } from 'react-native-appcycle';

function App() {
  return (
    <OverlayProvider>
      <YourApp />
      <OverlayContainer />
    </OverlayProvider>
  );
}
```

- `OverlayProvider`: holds overlay visibility state, “overlay-only” mode, and app API.
- `OverlayContainer`: renders the overlay modal (BottomSheet) when visible; must be mounted inside `OverlayProvider`.

### 2. Register overlay content and app API

So the overlay can render your UI and call into your app (e.g. API client, DB), register a content component and an API object:

```tsx
import { useOverlay, DefaultOverlayContent } from 'react-native-appcycle';

function SetupOverlay() {
  const { setAppApi, registerOverlayContent } = useOverlay();

  useEffect(() => {
    setAppApi({
      fetchExample: async () => ({ data: 'from app' }),
      // ... your API methods
    });
    registerOverlayContent(DefaultOverlayContent); // or your own component
  }, [setAppApi, registerOverlayContent]);

  return null;
}
```

Use `DefaultOverlayContent` for a simple demo (title, Close, “Call API” that calls `appApi.fetchExample`), or pass your own component.

### 3. Show/hide overlay from your app

```tsx
import { useOverlay } from 'react-native-appcycle';

function MyScreen() {
  const overlay = useOverlay();

  return (
    <Button
      title="Show overlay"
      onPress={() => overlay.open()}
    />
  );
}
```

- `overlay.open()`: sets overlay visible and shows the BottomSheet (same window).
- `overlay.openAndBringToFront()`: sets visible and calls native `openOverlay()` so the app is brought to front (e.g. from background).
- `overlay.close()`: hides overlay and, in overlay-only mode, finishes the overlay-only activity when applicable.

### 4. Overlay-only mode (e.g. launched from assistant)

When the app is started with `openOverlayOnly=true`, the library sets `overlayOnlyMode` to `true`. Your root UI can branch and render only the overlay (e.g. transparent background + `OverlayContainer`):

```tsx
function AppRoot() {
  const { overlayOnlyMode } = useOverlay();

  if (overlayOnlyMode) {
    return (
      <View style={{ flex: 1, backgroundColor: 'transparent' }}>
        <OverlayContainer />
      </View>
    );
  }
  return (
    <>
      <YourMainApp />
      <OverlayContainer />
    </>
  );
}
```

Inside the overlay, use `requestFullApp()` when the user taps “Open full app”; then switch to your main Activity (e.g. via `NativeModules.AppcycleOverlay.openFullApp()`). The example app wires this flow.

### 5. Initialize and lifecycle (Android)

Call `init()` once when your app is ready (e.g. after the Activity is attached):

```tsx
import Appcycle from 'react-native-appcycle';

useEffect(() => {
  Appcycle.init();
}, []);
```

Optional: start the **runtime** (foreground service) so you can trigger the overlay from background and receive heartbeat events:

```tsx
// Start runtime (request notification permission on Android 13+ first)
Appcycle.startRuntime();

// Stop runtime
Appcycle.stopRuntime();
```

Subscribe to lifecycle and heartbeat events:

```tsx
import { addEventListener, AppcycleEvents } from 'react-native-appcycle';

const unsubscribe = addEventListener('onForeground', () => { /* app came to foreground */ });
const unsubBackground = addEventListener('onBackground', () => { /* app went to background */ });
const unsubLive = addEventListener('onIsLive', () => { /* heartbeat from runtime (Android) */ });

// Later: unsubscribe();
```

### 6. Open overlay from background (Android)

- **From JS (demo)**: Call `triggerShowOverlayFromBackground()`. The runtime service (if running) will start your main Activity with `openOverlay=true`; your app comes to front and `OverlayProvider` will show the overlay.
- **From notification**: The library’s runtime service notification includes an “Open overlay” action that does the same. You can also start the Activity yourself with the same intent (`com.appcycle.SHOW_OVERLAY` + extra `openOverlay=true`).

### 7. Default assistant settings (Android)

To send the user to the system screen where they can set your app as the default digital assistant (where supported):

```tsx
import { openDefaultAssistantSettings } from 'react-native-appcycle';

openDefaultAssistantSettings();
```

---

## API reference

### Core

| Method / export              | Description |
|-----------------------------|-------------|
| `init()`                    | Initialize native state. Call once when the app/Activity is ready (Android). |
| `startRuntime()`            | (Android) Start the foreground runtime service (heartbeat + “trigger overlay from background”). Start from foreground on Android 12+. |
| `stopRuntime()`             | (Android) Stop the runtime service. |
| `triggerShowOverlayFromBackground()` | (Android) Ask the runtime service to start the app with `openOverlay=true` (demo). |
| `addEventListener(eventName, handler)` | Subscribe to `onForeground`, `onBackground`, or `onIsLive`. Returns an unsubscribe function. |
| `AppcycleEvents`            | `{ onForeground, onBackground, onIsLive }`. |

### Overlay (native layer)

| Method / export              | Description |
|-----------------------------|-------------|
| `openOverlay()`             | (Android) Start main Activity with overlay intent (bring app to front). |
| `closeOverlay()`            | (Android) Emit close event so JS can hide overlay; does not finish Activity. |
| `getAndClearLaunchExtra(key)` | (Android) Read and consume a boolean launch extra (e.g. `openOverlay`, `openOverlayOnly`). Returns `Promise<boolean>`. |
| `openDefaultAssistantSettings()` | (Android) Open system Settings for default voice/assistant. |

### React components and hooks

| Export               | Description |
|----------------------|-------------|
| `OverlayProvider`    | Context provider for overlay state, app API, and overlay content. Wrap your app (or the subtree that uses overlay). |
| `OverlayContainer`   | Renders the overlay modal (BottomSheet) when `visible`; must be used inside `OverlayProvider`. Content is the component passed to `registerOverlayContent`. |
| `DefaultOverlayContent` | Simple demo overlay (title, Close, “Call API” using `appApi.fetchExample`). |
| `useOverlay()`       | Returns `{ visible, open, openAndBringToFront, close, overlayOnlyMode, requestFullApp, appApi, setAppApi, OverlayContent, registerOverlayContent }`. Throws if used outside `OverlayProvider`. |
| `useAppApi()`        | Returns the app API object (for use inside overlay content). |
| `AppApi`             | Type: `Record<string, unknown>` for the app-provided API. |

### Event names

- `onForeground`: app entered foreground (Android + iOS where implemented).
- `onBackground`: app entered background.
- `onIsLive`: periodic heartbeat from the runtime service (Android, when runtime is running).

---

## Example app

The repo includes an **example** app (React Native 0.83) that demonstrates:

- `OverlayProvider` + `OverlayContainer` + `DefaultOverlayContent`
- Registering `appApi` and overlay content
- Start/stop runtime, “Show overlay”, “Trigger overlay from background”
- Opening default assistant settings
- Overlay-only mode and “Open full app” (with optional `OverlayOnlyActivity` and voice services)

From the repo root:

```bash
yarn install
yarn example android   # or: cd example && npx react-native run-android
yarn example ios      # or: cd example && npx react-native run-ios
```

The example uses a local `react-native.config.js` so the app depends on the parent library from the monorepo.

---

## Building the library

```bash
yarn install
yarn prepare    # builds JS/TS (react-native-builder-bob)
yarn typecheck
yarn lint
yarn test
```

---

## License

MIT. See [LICENSE](LICENSE).

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). Please read [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) as well.
