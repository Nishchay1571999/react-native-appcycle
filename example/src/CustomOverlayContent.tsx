import { useState } from 'react';
import { Button, StyleSheet, Text, View } from 'react-native';
import { useAppApi, useOverlay } from 'react-native-appcycle';

/** Custom overlay content (BottomSheet) – registered in index.js. */
export function CustomOverlayContent() {
  const { close } = useOverlay();
  const appApi = useAppApi();
  const [result, setResult] = useState<string | null>(null);

  const handleCallApi = async () => {
    setResult(null);
    const fetchExample = appApi?.fetchExample as (() => Promise<{ data?: string }>) | undefined;
    if (typeof fetchExample !== 'function') {
      setResult('No app API provided');
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
      <Text style={styles.title}>Custom Modal</Text>
      <View style={styles.buttons}>
        <Button title="Close" onPress={close} />
        <View style={{ width: 12 }} />
        <Button title="Call API" onPress={handleCallApi} />
      </View>
      {result != null ? <Text style={styles.result}>{result}</Text> : null}
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
