import { useState } from 'react';
import { Button, StyleSheet, Text, View } from 'react-native';
import { useAppApi } from './OverlayContext';
import { useOverlay } from './OverlayContext';

/**
 * Default overlay content: title, Close button, and "Call API" that runs app-provided fetchExample.
 * The example app can use this or register its own component via registerOverlayContent().
 */
export function DefaultOverlayContent() {
  const { close } = useOverlay();
  const appApi = useAppApi();
  const [result, setResult] = useState<string | null>(null);


  const handleCallApi = async () => {
    setResult(null);
    const fetchExample = appApi?.fetchExample as (() => Promise<{ data?: string }>) | undefined;
    if (typeof fetchExample !== 'function') {
      setResult('No app API (fetchExample) provided');
      return;
    }
    try {
      const res = await fetchExample();
      setResult(JSON.stringify(res ?? {}));
    } catch (e) {
      setResult(String(e));
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Assistant</Text>
      <View style={styles.buttons}>
        <Button title="Close" onPress={close} />
        <View style={{ width: 12 }} />
        <Button title="Call API" onPress={handleCallApi} />
      </View>
      {result != null ? (
        <Text style={styles.result}>{result}</Text>
      ) : null}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    padding: 24,
  },
  title: {
    fontSize: 20,
    fontWeight: '600',
    marginBottom: 16,
  },
  buttons: {
    flexDirection: 'row',
    marginBottom: 16,
  },
  result: {
    fontSize: 14,
    color: '#333',
  },
});
