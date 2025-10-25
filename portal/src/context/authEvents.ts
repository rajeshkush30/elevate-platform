// Lightweight event bus to decouple axios interceptors from React context
// Avoids circular imports by providing a simple subscribe/emit API

type Listener = () => void;

const logoutListeners: Set<Listener> = new Set();

export function onLogoutRequest(listener: Listener) {
  logoutListeners.add(listener);
  return () => logoutListeners.delete(listener);
}

export function emitLogoutRequest() {
  logoutListeners.forEach((l) => {
    try { l(); } catch {}
  });
}
