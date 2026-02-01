import { AppRegistry } from 'react-native';
import { registerOverlayContent, setDefaultAppApi } from 'react-native-appcycle';
import App, { CustomOverlayContent } from './src/App';
import { name as appName } from './app.json';

// Registry-style: register custom overlay (from App.tsx) and app API at entry.
async function fetchExample() {
  await new Promise((r) => setTimeout(r, 300));
  return { data: 'mock data from app' };
}
registerOverlayContent(CustomOverlayContent);
// setDefaultAppApi({ fetchExample });

AppRegistry.registerComponent(appName, () => App);
