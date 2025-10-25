import React, { useEffect, useState } from 'react';
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField, Box } from '@mui/material';

interface Props {
  open: boolean;
  onClose: () => void;
  onSave: (payload: any) => Promise<void>;
  initial?: any;
}

const ClientForm: React.FC<Props> = ({ open, onClose, onSave, initial }) => {
  const [email, setEmail] = useState('');
  const [firstName, setFirstName] = useState('');
  const [errors, setErrors] = useState<{ email?: string; firstName?: string }>({});
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    setEmail(initial?.email || '');
    setFirstName(initial?.firstName || initial?.fullName || '');
    setErrors({});
    setSaving(false);
  }, [initial, open]);

  const validate = () => {
    const e: any = {};
    if (!email) e.email = 'Email is required';
    else if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email)) e.email = 'Invalid email';
    if (!firstName) e.firstName = 'Name is required';
    setErrors(e);
    return Object.keys(e).length === 0;
  };

  const handleSave = async () => {
    if (!validate()) return;
    setSaving(true);
    try {
      await onSave({ email, firstName });
      onClose();
    } catch (err) {
      // let caller handle errors
    } finally {
      setSaving(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>{initial ? 'Edit Client' : 'Create Client'}</DialogTitle>
      <DialogContent>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 1 }}>
          <TextField label="Email" value={email} onChange={(e) => setEmail(e.target.value)} error={!!errors.email} helperText={errors.email} fullWidth />
          <TextField label="Full name" value={firstName} onChange={(e) => setFirstName(e.target.value)} error={!!errors.firstName} helperText={errors.firstName} fullWidth />
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={saving}>Cancel</Button>
        <Button variant="contained" onClick={handleSave} disabled={saving}>{saving ? 'Saving...' : 'Save'}</Button>
      </DialogActions>
    </Dialog>
  );
};

export default ClientForm;
