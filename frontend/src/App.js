// src/App.js
import React, { Suspense, lazy } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import CustomLoader from './components/CustomLoader';
import PrivateRoute from './components/PrivateRoute';
import PublicRoute from './components/PublicRoute';
const LoginPage = lazy(() => import('./pages/LoginPage'));
const DashboardPage = lazy(() => import('./pages/DashboardPage'));
const SignupPage = lazy(() => import('./pages/SignupPage'));
const ForgotPasswordPage = lazy(() => import('./pages/ForgotPasswordPage'));
const VMsPage = lazy(() => import('./pages/VMsPage'));
const VerifyPage = lazy(() => import('./pages/VerifyPage'));
const CloudsPage = lazy(() => import('./pages/CloudsPage'));
const ResetPasswordPage = lazy(() => import('./pages/ResetPass'));
const ChangePasswordPage = lazy(() => import('./pages/ChangePasswordPage'));
const ProfilePage = lazy(() => import('./pages/ProfilePage'));
const SettingsPage = lazy(() => import('./pages/SettingsPage'));
const NotFoundPage = lazy(() => import('./pages/BrandEngaged404Page'));
const ContactUsPage = lazy(() => import('./pages/ContactUsPage'));

const theme = createTheme({
  palette: {
    primary: { main: '#00445d' },
    secondary: { main: '#88b5c4' },
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <Suspense fallback={<CustomLoader />}>
        <Routes>
          <Route element={<PublicRoute />}>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/signup" element={<SignupPage />} />
            <Route path="/verify" element={<VerifyPage />} />
            <Route path="/forgot-password" element={<ForgotPasswordPage />} />
            <Route path="/reset-password" element={<ResetPasswordPage />} />
            <Route path="/change-password" element={<ChangePasswordPage />} />
          </Route>
          <Route path="/contact-us" element={<ContactUsPage />} />

          <Route element={<PrivateRoute />}>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/vms" element={<VMsPage />} />
            <Route path="/clouds" element={<CloudsPage />} />
            <Route path="/profile" element={<ProfilePage />} />
            <Route path="/settings" element={<SettingsPage />} />
          </Route>
          <Route path="/" element={<Navigate to="/dashboard" />} />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </Suspense>
    </ThemeProvider>
  );
}

export default App;
