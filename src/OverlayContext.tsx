import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
  type ReactNode,
} from 'react';
import { AppState, NativeEventEmitter, NativeModules, Platform } from 'react-native';
import NativeOverlay from './NativeAppcycleOverlay';

/** App-provided API (API client, DB, etc.) so the overlay can run app logic. */
export type AppApi = Record<string, unknown>;

type OverlayContextValue = {
  visible: boolean;
  open: () => void;
  /** Opens overlay and brings the app to front (starts main Activity with openOverlay). */
  openAndBringToFront: () => void;
  close: () => void;
  /** True when the app was launched with openOverlayOnly (e.g. from assistant)—show only the overlay, no full app UI. */
  overlayOnlyMode: boolean;
  /** Call when the user wants to open the full app from overlay-only mode (e.g. "Open full app" button). */
  requestFullApp: () => void;
  appApi: AppApi | null;
  setAppApi: (api: AppApi | null) => void;
  OverlayContent: React.ComponentType | null;
  registerOverlayContent: (Component: React.ComponentType) => void;
};

const OverlayContext = createContext<OverlayContextValue | null>(null);

const OVERLAY_CLOSE_EVENT = 'onOverlayCloseRequested';
const LAUNCH_EXTRA_OPEN_OVERLAY = 'openOverlay';
const LAUNCH_EXTRA_OPEN_OVERLAY_ONLY = 'openOverlayOnly';

export function OverlayProvider({ children }: { children: ReactNode }) {
  const [visible, setVisible] = useState(false);
  const [overlayOnlyMode, setOverlayOnlyMode] = useState(false);
  const [appApi, setAppApiState] = useState<AppApi | null>(null);
  const [OverlayContent, setOverlayContent] = useState<React.ComponentType | null>(null);
  const mountedRef = useRef(true);
  const overlayOnlyModeRef = useRef(false);

  const open = useCallback(() => {
    setVisible(true);
  }, []);

  /** Brings the app to front and shows the overlay (e.g. from background/notification). */
  const openAndBringToFront = useCallback(() => {
    setVisible(true);
    if (Platform.OS === 'android') NativeOverlay.openOverlay();
  }, []);

  const close = useCallback(() => {
    setVisible(false);
    if (Platform.OS === 'android') {
      NativeOverlay.closeOverlay();
      if (overlayOnlyMode) {
        NativeOverlay.finishActivity();
      }
    }
  }, [overlayOnlyMode]);

  const setAppApi = useCallback((api: AppApi | null) => {
    setAppApiState(api);
    if (typeof global !== 'undefined') {
      (global as Record<string, unknown>).__OVERLAY_APP_API__ = api;
    }
  }, []);

  const registerOverlayContent = useCallback((Component: React.ComponentType) => {
    setOverlayContent(() => Component);
  }, []);

  const requestFullApp = useCallback(() => {
    setOverlayOnlyMode(false);
  }, []);

  overlayOnlyModeRef.current = overlayOnlyMode;

  // Listen for native "close overlay" request
  useEffect(() => {
    if (Platform.OS !== 'android') return () => {};
    const emitter = new NativeEventEmitter(NativeModules.AppcycleOverlay);
    const sub = emitter.addListener(OVERLAY_CLOSE_EVENT, () => {
      if (mountedRef.current) {
        setVisible(false);
        if (overlayOnlyModeRef.current) NativeOverlay.finishActivity();
      }
    });
    return () => sub.remove();
  }, []);

  // On mount and app state change: check launch intent for openOverlay (e.g. from background trigger or assistant)
  useEffect(() => {
    if (Platform.OS !== 'android') return;
    const checkLaunchExtra = () => {
      NativeOverlay.getAndClearLaunchExtra(LAUNCH_EXTRA_OPEN_OVERLAY).then((shouldOpen) => {
        if (!mountedRef.current || !shouldOpen) return;
        setVisible(true);
        NativeOverlay.getAndClearLaunchExtra(LAUNCH_EXTRA_OPEN_OVERLAY_ONLY).then((overlayOnly) => {
          if (mountedRef.current && overlayOnly) setOverlayOnlyMode(true);
        });
      });
    };
    checkLaunchExtra();
    const sub = AppState.addEventListener('change', (state) => {
      if (state === 'active') checkLaunchExtra();
    });
    return () => sub.remove();
  }, []);

  useEffect(() => {
    mountedRef.current = true;
    return () => {
      mountedRef.current = false;
    };
  }, []);

  const value: OverlayContextValue = {
    visible,
    open,
    openAndBringToFront,
    close,
    overlayOnlyMode,
    requestFullApp,
    appApi,
    setAppApi,
    OverlayContent,
    registerOverlayContent,
  };

  return (
    <OverlayContext.Provider value={value}>
      {children}
    </OverlayContext.Provider>
  );
}

export function useOverlay(): OverlayContextValue {
  const ctx = useContext(OverlayContext);
  if (!ctx) throw new Error('useOverlay must be used inside OverlayProvider');
  return ctx;
}

/** Hook for overlay content to access the app-provided API (fetch, DB, etc.). */
export function useAppApi(): AppApi | null {
  const ctx = useContext(OverlayContext);
  if (!ctx) return null;
  return ctx.appApi ?? (typeof global !== 'undefined' ? (global as Record<string, unknown>).__OVERLAY_APP_API__ as AppApi ?? null : null);
}
