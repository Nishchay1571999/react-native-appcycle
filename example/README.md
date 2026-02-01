# Appcycle Example

Example app for [react-native-appcycle](../README.md). Run from the **monorepo root**:

```bash
yarn install
yarn example android   # or: cd example && npx react-native run-android
yarn example ios      # or: cd example && npx react-native run-ios
```

## What it demonstrates

- **Registry-style setup**: `registerOverlayContent(CustomOverlayContent)` and `setDefaultAppApi({ fetchExample })` in `index.js`; custom overlay in `src/CustomOverlayContent.tsx`.
- **OverlayProvider** wrapping the app (overlay rendered internally).
- Start/stop runtime, “Show overlay”, “Trigger overlay from background”.
- Opening default assistant settings (long-press home).
- Overlay-only mode and “Open full app” (optional `OverlayOnlyActivity` and voice services on Android).

## Structure

- `index.js` – Entry; registers overlay content and app API.
- `src/App.tsx` – Root: `OverlayProvider` + `MainScreen`.
- `src/MainScreen.tsx` – Main UI (runtime, overlay buttons, assistant settings).
- `src/CustomOverlayContent.tsx` – Custom overlay (BottomSheet) content; registered in `index.js`.
