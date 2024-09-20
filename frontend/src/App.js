import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import SignupPage from './pages/SignupPage';
import ForgotPage from './pages/ForgotPage'
import VMsPage from './pages/VMsPage';
import VerifyPage from './pages/VerifyPage';
import CloudsPage from './pages/CloudsPage';
function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Navigate to="/login" />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/signup" element={<SignupPage/>} />
        <Route path="/verify" element={<VerifyPage/>} />
        <Route path="/dashboard" element={<DashboardPage/>} />
        <Route path="/forgot-password" element={<ForgotPage/>} />
        <Route path="/vms" element={<VMsPage/>} />
        <Route path="/clouds" element={<CloudsPage/>}/>
      </Routes>
    </Router>
  );
}

export default App;