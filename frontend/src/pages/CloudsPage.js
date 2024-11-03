import React, { useEffect } from 'react';
import Clouds from '../components/Clouds';

const CloudsPage = () => {
  useEffect(() => {
    document.title = 'C-Cloud | Clouds';
  }, []); 
  return <Clouds />;
};

export default CloudsPage;
