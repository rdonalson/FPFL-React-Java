// src/app/auth/LoginDialog.tsx
import { useState } from 'react';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { Password } from 'primereact/password';
import { Button } from 'primereact/button';
import { useNavigate } from 'react-router-dom';

import { loginApi } from '@/api/authApi';
import { useSessionStore } from '@/app/state/sessionStore';

export default function LoginDialog({
  visible,
  onClose,
}: {
  visible: boolean;
  onClose: () => void;
}) {
  const navigate = useNavigate();
  const setSession = useSessionStore(s => s.setSession);

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleLogin() {
    try {
      setLoading(true);

      // loginApi returns normalized:
      // { token, refreshToken, userId, email, roles, raw }
      const result = await loginApi(email, password);

      setSession(result);

      onClose();
      navigate('/');
    } catch (err) {
      console.error('Login failed', err);
    } finally {
      setLoading(false);
    }
  }

  return (
    <Dialog header="Login" visible={visible} onHide={onClose} style={{ width: '25rem' }}>
      <div className="flex flex-col gap-3">
        <span className="p-float-label">
          <InputText id="email" value={email} onChange={e => setEmail(e.target.value)} />
          <label htmlFor="email">Email</label>
        </span>

        <span className="p-float-label">
          <Password
            id="password"
            value={password}
            onChange={e => setPassword(e.target.value)}
            toggleMask
          />
          <label htmlFor="password">Password</label>
        </span>

        <Button label="Login" icon="pi pi-check" loading={loading} onClick={handleLogin} />
      </div>
    </Dialog>
  );
}
