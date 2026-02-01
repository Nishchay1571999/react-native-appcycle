import { useEffect, useRef, useState } from 'react';
import {
  View,
  Text,
  Button,
  PermissionsAndroid,
  type Permission,
  Platform,
  StyleSheet,
  Alert,
  Linking,
} from 'react-native';

import Appcycle, {
  OverlayProvider,
  OverlayContainer,
  DefaultOverlayContent,
  useOverlay,
  triggerShowOverlayFromBackground,
  openDefaultAssistantSettings,
} from 'react-native-appcycle';

const ANDROID_VERSION = Platform.OS === 'android' ? Platform.Version : 0;
const NEEDS_NOTIFICATION_PERMISSION = ANDROID_VERSION >= 33;

/** Dummy API function provided by the app so the overlay can call it (proves app API access). */
async function fetchExample(): Promise<{ data: string }> {
  await new Promise((r) => setTimeout(r, 300));
  return { data: 'mock data from app' };
}

function AppContent() {
  const [ready, setReady] = useState(false);
  const startedRef = useRef(false);
  const overlay = useOverlay();

  // Let React + Activity settle
  useEffect(() => {
    const id = setTimeout(() => {
      setReady(true);
    }, 300);

    return () => clearTimeout(id);
  }, []);

  // Init native AFTER activity exists
  useEffect(() => {
    if (!ready || startedRef.current) return;
    startedRef.current = true;

    Appcycle.init();
  }, [ready]);

  // Subscribe to "is live" heartbeat from background service (shows in JS console)
  useEffect(() => {
    const unsub = Appcycle.addEventListener('onIsLive', () => {
      console.log('is live');
    });
    return unsub;
  }, []);

  const openSettingsAndExplain = (permissionLabel: string) => {
    Alert.alert(
      'Permission required',
      `"${permissionLabel}" was denied or set to "Don't ask again". Please enable it in App settings.`,
      [
        { text: 'Cancel', style: 'cancel' },
        { text: 'Open settings', onPress: () => Linking.openSettings() },
      ]
    );
  };

  const requestPermission = async (
    permission: Permission,
    label: string
  ): Promise<boolean> => {
    if (Platform.OS !== 'android') return true;

    const alreadyGranted = await PermissionsAndroid.check(permission);
    if (alreadyGranted) return true;

    const result = await PermissionsAndroid.request(permission);
    console.log('permission result', permission, result);

    if (result === PermissionsAndroid.RESULTS.GRANTED) return true;
    if (
      result === PermissionsAndroid.RESULTS.NEVER_ASK_AGAIN ||
      result === PermissionsAndroid.RESULTS.DENIED
    ) {
      openSettingsAndExplain(label);
      return false;
    }
    return false;
  };

  const requestAllPermissions = async (): Promise<boolean> => {
    if (Platform.OS !== 'android') return true;

    const micOk = await requestPermission(
      PermissionsAndroid.PERMISSIONS.RECORD_AUDIO,
      'Microphone'
    );
    if (!micOk) return false;

    if (NEEDS_NOTIFICATION_PERMISSION) {
      const notifOk = await requestPermission(
        PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS,
        'Notifications'
      );
      if (!notifOk) return false;
    }

    return true;
  };

  const start = async () => {
    console.log('start');
    const ok = await requestAllPermissions();
    if (!ok) return;

    Appcycle.startRuntime();
  };

  const stop = () => {
    Appcycle.stopRuntime();
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Appcycle Example</Text>

      <Button title="Start Runtime" onPress={start} />
      <View style={{ height: 12 }} />
      <Button title="Stop Runtime" onPress={stop} />

      <View style={{ height: 24 }} />
      <Text style={styles.sectionTitle}>Overlay / BottomSheet</Text>
      <Button
        title="Show overlay"
        onPress={() => overlay.open()}
      />
      <View style={{ height: 12 }} />
      <Button
        title="Trigger overlay from background"
        onPress={() => triggerShowOverlayFromBackground()}
      />
      <Text style={styles.hint}>
        Start Runtime first, then tap &quot;Trigger overlay from background&quot; to
        simulate the service opening the overlay.
      </Text>
      <View style={{ height: 24 }} />
      <Text style={styles.sectionTitle}>Default assistant (long-press home)</Text>
      <Button
        title="Set as default assistant"
        onPress={() => openDefaultAssistantSettings()}
      />
      <Text style={styles.hint}>
        Opens Settings so you can choose this app as the default digital assistant.
        Then long-press home will open this app&apos;s overlay instead of Google Assistant.
      </Text>
      <Text style={styles.hint}>
        On many devices (Samsung, OnePlus, Xiaomi, etc.) the list shows only None and
        Google—third-party apps are not allowed. Use the Quick Settings tile or
        accessibility shortcut instead to open the overlay.
      </Text>

    </View>
  );
}

export default function App() {
  const [appApiSet, setAppApiSet] = useState(false);

  useEffect(() => {
    if (appApiSet) return;
    setAppApiSet(true);
  }, [appApiSet]);

  return (
    <OverlayProvider>
      <View style={{ flex: 1 }}>
        <OverlayContentWithApi />
        <AppRoot />
      </View>
    </OverlayProvider>
  );
}

/** When overlayOnlyMode (launched from assistant), show only the overlay on a transparent background. Otherwise show full app + overlay. */
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
      <AppContent />
      <OverlayContainer />
    </>
  );
}

/** Registers overlay content and sets app API once. */
function OverlayContentWithApi() {
  const { setAppApi, registerOverlayContent } = useOverlay();
  const registered = useRef(false);

  useEffect(() => {
    if (registered.current) return;
    registered.current = true;
    setAppApi({ fetchExample });
    registerOverlayContent(DefaultOverlayContent);
  }, [setAppApi, registerOverlayContent]);

  return null;
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  title: {
    fontSize: 18,
    marginBottom: 24,
  },
  sectionTitle: {
    fontSize: 16,
    marginTop: 8,
    marginBottom: 8,
  },
  hint: {
    fontSize: 12,
    color: '#666',
    marginTop: 12,
    paddingHorizontal: 24,
    textAlign: 'center',
  },
});
