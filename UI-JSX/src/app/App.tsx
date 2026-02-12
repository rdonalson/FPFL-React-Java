import { AppRouter } from '@/app/router/AppRouter';
import { PrimeReactProvider } from 'primereact/api';

function App() {
  return (
    <PrimeReactProvider value={{ ripple: true }}>
      <AppRouter />
    </PrimeReactProvider>
  );
}

export default App;
