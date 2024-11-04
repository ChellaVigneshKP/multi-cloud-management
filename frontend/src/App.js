import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import SignupPage from './pages/SignupPage';
import ForgotPasswordPage from './pages/ForgotPasswordPage';
import VMsPage from './pages/VMsPage';
import VerifyPage from './pages/VerifyPage';
import CloudsPage from './pages/CloudsPage';
import ResetPasswordPage from './pages/ResetPass';
import ChangePasswordPage from './pages/ChangePasswordPage';
import ProfilePage from './pages/ProfilePage';
import SettingsPage from './pages/SettingsPage';

const theme = createTheme({
  palette: {
    primary: {
      main: '#00445d',
    },
    secondary: {
      main: '#88b5c4',
    },
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <Router>
        <Routes>
          <Route path="/" element={<Navigate to="/login" />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route path="/verify" element={<VerifyPage />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password" element={<ResetPasswordPage />} />
          <Route path="/change-password" element={<ChangePasswordPage />} />
          <Route path="/vms" element={<VMsPage />} />
          <Route path="/clouds" element={<CloudsPage />} />
          <Route path="/profile" element={<ProfilePage />} />
          <Route path="/settings" element={<SettingsPage />} />
        </Routes>
      </Router>
    </ThemeProvider>
  );
}

export default App;