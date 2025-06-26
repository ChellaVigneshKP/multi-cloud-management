'use client';

import React, { useEffect } from 'react';
import VMs from '@/components/VMs';

const VMsPage = () => {
  useEffect(() => {
    document.title = 'C-Cloud | VMs';
  }, []);

  return <VMs />;
};

export default VMsPage;