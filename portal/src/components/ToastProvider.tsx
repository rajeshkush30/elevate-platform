import React, { createContext, useCallback, useContext, useMemo, useState } from 'react';
import { AlertColor, Snackbar, Alert } from '@mui/material';

type ToastContextType = {
  showToast: (message: string, severity?: AlertColor) => void;
};

const ToastContext = createContext<ToastContextType | undefined>(undefined);

// Allow non-React modules (e.g., axios http.ts) to trigger toasts
let externalToastImpl: ((message: string, severity?: AlertColor) => void) | null = null;
export const showGlobalToast = (message: string, severity: AlertColor = 'info') => {
  if (externalToastImpl) externalToastImpl(message, severity);
};

export const ToastProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [open, setOpen] = useState(false);
  const [message, setMessage] = useState('');
  const [severity, setSeverity] = useState<AlertColor>('info');

  const showToast = useCallback((msg: string, sev: AlertColor = 'info') => {
    setMessage(msg);
    setSeverity(sev);
    setOpen(true);
  }, []);

  const value = useMemo(() => ({ showToast }), [showToast]);

  // register global toast impl
  React.useEffect(() => {
    externalToastImpl = showToast;
    return () => { externalToastImpl = null; };
  }, [showToast]);

  return (
    <ToastContext.Provider value={value}>
      {children}
      <Snackbar open={open} autoHideDuration={3000} onClose={() => setOpen(false)} anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}>
        <Alert onClose={() => setOpen(false)} severity={severity} variant="filled" sx={{ width: '100%' }}>
          {message}
        </Alert>
      </Snackbar>
    </ToastContext.Provider>
  );
};

export const useToast = (): ToastContextType => {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast must be used within a ToastProvider');
  return ctx;
};
