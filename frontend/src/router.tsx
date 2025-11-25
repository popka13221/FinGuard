import { createBrowserRouter, Navigate } from 'react-router-dom';
import AuthPage from './screens/AuthPage';
import DashboardPage from './screens/DashboardPage';

export const AppRouter = createBrowserRouter([
  { path: '/', element: <Navigate to="/auth" replace /> },
  { path: '/auth', element: <AuthPage /> },
  { path: '/dashboard', element: <DashboardPage /> },
]);
