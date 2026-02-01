import { View } from 'react-native';
import { OverlayProvider } from 'react-native-appcycle';
import { MainScreen } from './MainScreen';

export default function App() {
  return (
    <OverlayProvider>
      <View style={{ flex: 1 }}>
        <MainScreen />
      </View>
    </OverlayProvider>
  );
}
