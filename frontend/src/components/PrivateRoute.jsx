// src/components/PrivateRoute.js
import React, { useContext } from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { AuthContext } from './AuthContext';
import CustomLoader from './CustomLoader';

const PrivateRoute = () => {
  const { isAuthenticated } = useContext(AuthContext);

  if (isAuthenticated === null) {
    // Authentication status is still loading
    return <CustomLoader />;
  }

  return isAuthenticated ? <Outlet /> : <Navigate to="/login" />;
};

export default PrivateRoute;
