import { useEffect, useState } from 'react';
import LoginPage from './page/LoginPage';
import SignUpPage from './page/SignUpPage';
import UploadDashboard from './page/UploadDashboard';
import VersionControl from './page/VersionControl';
import VersionDetail from './page/Version-Detail';

const LOGIN_ROUTE = '/login';
const SIGNUP_ROUTE = '/signup';
const DASHBOARD_ROUTE = '/dashboard';
const VERSION_CONTROL_ROUTE = '/version-control';
const VERSION_DETAIL_ROUTE = '/version-detail';

const resolveRoute = (pathname) => {
  if (
    pathname === DASHBOARD_ROUTE ||
    pathname === VERSION_CONTROL_ROUTE ||
    pathname === VERSION_DETAIL_ROUTE
  ) {
    return pathname;
  }
  if (pathname === SIGNUP_ROUTE) return SIGNUP_ROUTE;

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
    // Extract pathname without query string
    const pathname = route.split('?')[0];
    setCurrentRoute(pathname);
  };

  if (currentRoute === DASHBOARD_ROUTE) {
    return <UploadDashboard onLogout={() => navigateTo(LOGIN_ROUTE)} onNavigate={navigateTo} />;
  }

  if (currentRoute === VERSION_CONTROL_ROUTE) {
    return <VersionControl onNavigate={navigateTo} />;
  }

  if (currentRoute === VERSION_DETAIL_ROUTE) {
    return <VersionDetail onNavigate={navigateTo} />;
  }

  // return <LoginPage onSignIn={() => navigateTo(DASHBOARD_ROUTE)} />;
  return <UploadDashboard/>;
}

export default App;
