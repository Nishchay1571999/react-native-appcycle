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
  useOverlay,
  triggerShowOverlayFromBackground,
  openDefaultAssistantSettings,
} from 'react-native-appcycle';

const ANDROID_VERSION = Platform.OS === 'android' ? Platform.Version : 0;
const NEEDS_NOTIFICATION_PERMISSION = ANDROID_VERSION >= 33;

export function MainScreen() {
  const [ready, setReady] = useState(false);
  const startedRef = useRef(false);
  const overlay = useOverlay();

  useEffect(() => {
    const id = setTimeout(() => setReady(true), 300);
    return () => clearTimeout(id);
  }, []);

  useEffect(() => {
    if (!ready || startedRef.current) return;
    startedRef.current = true;
    Appcycle.init();
  }, [ready]);

  useEffect(() => {
    const unsub = Appcycle.addEventListener('onIsLive', () => console.log('is live'));
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
    if (await PermissionsAndroid.check(permission)) return true;
    const result = await PermissionsAndroid.request(permission);
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

  const startRuntime = async () => {
    const ok = await requestAllPermissions();
    if (!ok) return;
    Appcycle.startRuntime();
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Appcycle Example</Text>

      <Button title="Start Runtime" onPress={startRuntime} />
      <View style={{ height: 12 }} />
      <Button title="Stop Runtime" onPress={() => Appcycle.stopRuntime()} />

      <View style={{ height: 24 }} />
      <Text style={styles.sectionTitle}>Overlay / BottomSheet</Text>
      <Button title="Show overlay" onPress={() => overlay.open()} />
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
