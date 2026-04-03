import { useEffect, useState } from 'react';
import LoginPage from './page/LoginPage';
import UploadDashboard from './page/UploadDashboard';

const LOGIN_ROUTE = '/login';
const DASHBOARD_ROUTE = '/dashboard';

const resolveRoute = (pathname) => {
  if (pathname === DASHBOARD_ROUTE) {
    return DASHBOARD_ROUTE;
  }

  return LOGIN_ROUTE;
};

function App() {
  const [currentRoute, setCurrentRoute] = useState(() => resolveRoute(window.location.pathname));

  useEffect(() => {
    const normalized = resolveRoute(window.location.pathname);
    if (window.location.pathname !== normalized) {
      window.history.replaceState({}, '', normalized);
    }

    const handlePopState = () => {
      setCurrentRoute(resolveRoute(window.location.pathname));
    };

    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  const navigateTo = (route) => {
    if (window.location.pathname !== route) {
      window.history.pushState({}, '', route);
    }
    setCurrentRoute(route);
  };

  if (currentRoute === DASHBOARD_ROUTE) {
    return <UploadDashboard onLogout={() => navigateTo(LOGIN_ROUTE)} />;
  }

  return <LoginPage onSignIn={() => navigateTo(DASHBOARD_ROUTE)} />;
}

export default App;
