import { useEffect, useState } from 'react';
import LoginPage from './page/LoginPage';
import SignUpPage from './page/SignUpPage';
import UploadDashboard from './page/UploadDashboard';
import VersionControl from './page/VersionControl';
// import VersionDetail from './page/Version-Detail';
// import DocumentPreview from './page/DocumentPreview';
// import UpdateDocument from './page/UpdateDocument';
import DashboardPage from "./page/DashboardPage";

const LOGIN_ROUTE = '/login';
const SIGNUP_ROUTE = '/signup';
const DASHBOARD_ROUTE = '/upload';
const VERSION_CONTROL_ROUTE = '/version-control';
export const VERSION_DETAIL_ROUTE = '/version-detail';
const PREVIEW_ROUTE = '/preview';
const UPDATE_DOCUMENT_ROUTE = '/update-document';
const DASHBOARD = '/dashboard';

const resolveRoute = (pathname) => {
  if (
    pathname === SIGNUP_ROUTE ||
    pathname === DASHBOARD_ROUTE ||
    pathname === VERSION_CONTROL_ROUTE ||
    pathname === VERSION_DETAIL_ROUTE ||
    pathname === PREVIEW_ROUTE ||
    pathname === UPDATE_DOCUMENT_ROUTE ||
    pathname === DASHBOARD
  ) {
    return pathname;
  }
  return DASHBOARD_ROUTE;
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

  if (currentRoute === SIGNUP_ROUTE) {
    return <SignUpPage onNavigate={() => navigateTo(LOGIN_ROUTE)} />;
  }
  if (currentRoute === DASHBOARD_ROUTE) {
    return <UploadDashboard onLogout={() => navigateTo(LOGIN_ROUTE)} onNavigate={navigateTo} />;
  }
  if (currentRoute === VERSION_CONTROL_ROUTE) {
    return <VersionControl onNavigate={navigateTo} />;
  }
  // if (currentRoute === VERSION_DETAIL_ROUTE) {
  //   return <VersionDetail onNavigate={navigateTo} />;
  // }
  // if (currentRoute === PREVIEW_ROUTE) {
  //   return <DocumentPreview onNavigate={navigateTo} />;
  // }
  // if (currentRoute === UPDATE_DOCUMENT_ROUTE) {
  //   return <UpdateDocument onNavigate={navigateTo} doc={selectedDoc} />;
  // }
  if (currentRoute === DASHBOARD) {
    return <DashboardPage onNavigate={navigateTo} />;
  }

  // return <LoginPage onSignIn={() => navigateTo(DASHBOARD_ROUTE)} onNavigateSignUp={() => navigateTo(SIGNUP_ROUTE)} />;



  return <DashboardPage onNavigate={navigateTo} />;
}

export default App;
