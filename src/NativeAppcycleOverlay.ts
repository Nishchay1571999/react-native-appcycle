import { NativeModules, Platform } from 'react-native';

export interface AppcycleOverlayNativeModule {
  openOverlay(): void;
  closeOverlay(): void;
  getAndClearLaunchExtra(key: string): Promise<boolean>;
  /** Opens system Settings so the user can set this app as the default assistant (Android). */
  openDefaultAssistantSettings(): void;
  /** Finishes the current activity and starts the main app (use from overlay-only mode for "Open full app"). */
  openFullApp(): void;
  /** Finishes the current activity (use when overlay is closed in overlay-only mode so the app is removed from background). */
  finishActivity(): void;
}

const LINKING_ERROR =
  `The package 'appcycle' overlay isn't linked correctly.\n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const Native: AppcycleOverlayNativeModule =
  NativeModules.AppcycleOverlay
    ? NativeModules.AppcycleOverlay
    : new Proxy(
        {} as AppcycleOverlayNativeModule,
        {
          get() {
            throw new Error(LINKING_ERROR);
          },
        }
      );

export default Native;
