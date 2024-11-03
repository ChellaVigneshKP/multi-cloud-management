import React, { useEffect } from 'react';
import Dashboard from '../components/Dashboard';

const DashboardPage = () => {
  useEffect(() => {
    document.title = 'C-Cloud | Dashboard';
  }, []);
  return (
    <div>
      <Dashboard />
    </div>
  );
};

export default DashboardPage;