import { Modal, Pressable, StyleSheet } from 'react-native';
import { useOverlay } from './OverlayContext';

/**
 * Renders the overlay (BottomSheet-style) when visible. Must be mounted inside OverlayProvider.
 * The app registers overlay content via registerOverlayContent(); that component runs in the
 * same React tree and can use useAppApi() to call app endpoints.
 */
export function OverlayContainer() {
  const { visible, close, OverlayContent } = useOverlay();

  if (!visible) return null;

  return (
    <Modal
      visible={visible}
      transparent
      animationType="slide"
      onRequestClose={close}
      statusBarTranslucent
    >
      <Pressable style={styles.backdrop} onPress={close} accessibilityLabel="Close overlay">
        <Pressable style={styles.sheet} onPress={(e) => e.stopPropagation()}>
          {OverlayContent ? <OverlayContent /> : null}
        </Pressable>
      </Pressable>
    </Modal>
  );
}

const styles = StyleSheet.create({
  backdrop: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.4)',
    justifyContent: 'flex-end',
  },
  sheet: {
    backgroundColor: '#fff',
    borderTopLeftRadius: 16,
    borderTopRightRadius: 16,
    paddingBottom: 32,
    minHeight: 200,
  },
});
