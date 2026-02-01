import { NativeModules, Platform } from 'react-native';

export interface AppcycleNativeModule {
  init(): void;
  startRuntime(): void;
  stopRuntime(): void;
  triggerShowOverlayFromBackground(): void;
  addListener(eventName: string): void;
  removeListeners(count: number): void;
}

const LINKING_ERROR =
  `The package 'appcycle' doesn't seem to be linked correctly.\n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const Native: AppcycleNativeModule =
  NativeModules.Appcycle
    ? NativeModules.Appcycle
    : new Proxy(
        {},
        {
          get() {
            throw new Error(LINKING_ERROR);
          },
        }
      );

export default Native;
