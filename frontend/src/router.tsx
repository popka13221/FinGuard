import { createBrowserRouter, Navigate } from 'react-router-dom';
import AuthPage from './screens/AuthPage';
import ForgotPage from './screens/ForgotPage';
import ResetPage from './screens/ResetPage';
import DashboardPage from './screens/DashboardPage';

export const AppRouter = createBrowserRouter([
  { path: '/', element: <Navigate to="/auth" replace /> },
  { path: '/auth', element: <AuthPage /> },
  { path: '/forgot', element: <ForgotPage /> },
  { path: '/reset', element: <ResetPage /> },
  { path: '/dashboard', element: <DashboardPage /> },
]);
