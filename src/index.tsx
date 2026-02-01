import { NativeEventEmitter, NativeModules, Platform } from 'react-native';
import Native from './NativeAppcycle';
import NativeOverlay from './NativeAppcycleOverlay';

export function init() {
  return Native.init();
}

export function startRuntime() {
  return Native.startRuntime();
}

export function stopRuntime() {
  return Native.stopRuntime();
}

/** For demo: triggers overlay from background (runtime service starts app with openOverlay=true). */
export function triggerShowOverlayFromBackground() {
  if (Platform.OS === 'android') Native.triggerShowOverlayFromBackground();
}

/** Event names emitted by native (foreground/background + heartbeat). */
export const AppcycleEvents = {
  onForeground: 'onForeground',
  onBackground: 'onBackground',
  onIsLive: 'onIsLive',
} as const;

/** Subscribe to native events (e.g. onIsLive for "is live" heartbeat). Call the returned function to unsubscribe. */
export function addEventListener(
  eventName: keyof typeof AppcycleEvents,
  handler: () => void
): () => void {
  if (Platform.OS !== 'android') return () => {};
  const name = AppcycleEvents[eventName];
  const emitter = new NativeEventEmitter(NativeModules.Appcycle);
  const sub = emitter.addListener(name, handler);
  return () => sub.remove();
}

// ---------- Overlay API ----------

export {
  OverlayProvider,
  useOverlay,
  useAppApi,
  type AppApi,
} from './OverlayContext';
export { OverlayContainer } from './OverlayContainer';
export { DefaultOverlayContent } from './DefaultOverlayContent';

export function openOverlay() {
  if (Platform.OS === 'android') NativeOverlay.openOverlay();
  // Visible state is set by OverlayProvider when it receives launch extra or when app calls open() from context
  // For in-app open we need the provider to expose open() - so apps use useOverlay().open() or we emit
  // We don't have ref to provider here; so openOverlay() from JS should be used together with OverlayProvider
  // which listens for getAndClearLaunchExtra. So when user taps "Show overlay" in app, they should call
  // useOverlay().open() which both sets visible and calls native openOverlay(). So we export open/close from context.
  // For convenience we can have openOverlay() call native only - and the app must use OverlayProvider and call
  // open() from context to set visible. Actually useOverlay().open() already calls NativeOverlay.openOverlay() AND setVisible(true).
  // So openOverlay() standalone would only start the activity - useful when we want to "bring app to front and show overlay".
  // So we keep openOverlay() as native-only for "bring to front"; the app uses useOverlay().open() for "show overlay now".
}

export function closeOverlay() {
  if (Platform.OS === 'android') NativeOverlay.closeOverlay();
}

export function getAndClearLaunchExtra(key: string): Promise<boolean> {
  if (Platform.OS !== 'android') return Promise.resolve(false);
  return NativeOverlay.getAndClearLaunchExtra(key);
}

/** Opens system Settings so the user can set this app as the default assistant (Android). When set, long-press home will invoke your app and open the overlay. */
export function openDefaultAssistantSettings() {
  if (Platform.OS === 'android') NativeOverlay.openDefaultAssistantSettings();
}

export default {
  init,
  startRuntime,
  stopRuntime,
  triggerShowOverlayFromBackground,
  addEventListener,
  AppcycleEvents,
  openOverlay,
  closeOverlay,
  getAndClearLaunchExtra,
  openDefaultAssistantSettings,
};
