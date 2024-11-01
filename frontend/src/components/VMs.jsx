import React, { useState, useEffect } from 'react';
import {
  Box, Button, Checkbox, FormControlLabel, IconButton, Tooltip,
  Select, MenuItem, InputLabel, FormControl, Typography
} from '@mui/material';
import { DataGrid } from '@mui/x-data-grid';
import RefreshIcon from '@mui/icons-material/Refresh';
import FiberManualRecordIcon from '@mui/icons-material/FiberManualRecord';
import Layout from './Layout';
import api from '../api';
import noVmsImage from '../assets/images/avatars/vm-not-found.png'
import vmLoading from '../assets/images/avatars/cloud-hosting-animate.svg'
import awsLogo from '../assets/images/logo/aws-logo.png'
import azureLogo from '../assets/images/logo/azure-logo.png'
import gcpLogo from '../assets/images/logo/gcp-logo.png'

const VMs = () => {
  const [instances, setInstances] = useState([]);
  const [selectedInstances, setSelectedInstances] = useState([]);
  const [showTerminated, setShowTerminated] = useState(false);
  const [selectedAction, setSelectedAction] = useState('');
  const [loading, setLoading] = useState(true);
  const [gridHeight, setGridHeight] = useState(600);

  const fetchInstances = async () => {
    setLoading(true);
    try {
      const response = await api.get('/vm/aws/ec2');
      const data = response.data;
      setInstances(data.instances || []);
    } catch (error) {
      console.error('Error fetching instances:', error);
    } finally {
      setLoading(false);
    }
  };  

  useEffect(() => {
    fetchInstances();
  }, []);

  useEffect(() => {
    const calculateHeight = () => {
      const headerOffset = document.getElementById('grid-header')?.offsetHeight || 0;
      const windowHeight = window.innerHeight;
      const padding = 162; // Total vertical padding/margin in your layout (adjust as needed)
      setGridHeight(windowHeight - headerOffset - padding);
    };

    calculateHeight(); // Initial calculation

    window.addEventListener('resize', calculateHeight);
    return () => window.removeEventListener('resize', calculateHeight);
  }, []);

  const handleRefresh = () => {
    fetchInstances();
  };

  const handleActionChange = (action) => {
    console.log(`Performing ${action} on selected instances:`, selectedInstances);
    setSelectedAction('');
  };

  const handleShowTerminatedChange = (e) => {
    setShowTerminated(e.target.checked);
  };

  const filteredInstances = instances.filter((instance) => {
    if (!showTerminated && instance.state === 'terminated') return false;
    return true;
  });

  const totalInstances = instances.length;
  const runningInstances = instances.filter((instance) => instance.state === 'running').length;

  const stateColors = {
    running: '#00e676',
    stopped: '#ff5252',
    terminated: '#bdbdbd',
    starting: '#ffeb3b',
    stopping: '#ffeb3b',
  };

  const hasRunning = selectedInstances.some(
    (id) => instances.find((inst) => inst.instanceId === id)?.state === 'running'
  );
  const hasStopped = selectedInstances.some(
    (id) => instances.find((inst) => inst.instanceId === id)?.state === 'stopped'
  );

  // Define columns for DataGrid
  const columns = [
    {
      field: 'checkbox',
      headerName: '',
      sortable: false,
      renderHeader: () => null, // Remove header for checkbox column
      renderCell: (params) => (
        <Checkbox
          checked={selectedInstances.includes(params.row.instanceId)}
          onChange={() => {
            const selectedIds = [...selectedInstances];
            if (selectedIds.includes(params.row.instanceId)) {
              const index = selectedIds.indexOf(params.row.instanceId);
              selectedIds.splice(index, 1);
            } else {
              selectedIds.push(params.row.instanceId);
            }
            setSelectedInstances(selectedIds);
          }}
          size="small"
        />
      ),
      align: 'center',
      headerAlign: 'center',
      width: 40,
      minWidth: 40,
      flex: 0,
    },
    {
      field: 'provider',
      headerName: 'Provider',
      renderCell: (params) => {
        const provider = params.value;
    
        // Define logo based on provider name
        const logoSrc = provider === 'AWS' ? awsLogo
                     : provider === 'Azure' ? azureLogo
                     : provider === 'GCP' ? gcpLogo
                     : null;
    
        return (
          <Tooltip title={provider}>
            <Box display="flex" alignItems="center">
              {logoSrc && (
                <img
                  src={logoSrc}
                  alt={`${provider} logo`}
                  style={{ width: '30px', height: '20px', marginRight: '4px' }}
                />
              )}
              <Typography variant="body2" noWrap>{provider}</Typography>
            </Box>
          </Tooltip>
        );
      },
      align: 'center',
      headerAlign: 'center',
      minWidth: 30,
      flex: 1,
    },
    {
      field: 'keyId',
      headerName: 'KeyID',
      renderCell: (params) => (
        <Tooltip title={params.value}>
          <Typography variant="body2" noWrap>{params.value}</Typography>
        </Tooltip>
      ),
      align: 'center',
      headerAlign: 'center',
      minWidth: 60,
      flex: 1,
    },
    {
      field: 'name',
      headerName: 'Instance Name',
      renderCell: (params) => (
        <Tooltip title={params.value}>
          <Typography variant="body2" noWrap>{params.value}</Typography>
        </Tooltip>
      ),
      align: 'center',
      headerAlign: 'center',
      minWidth: 120,
      flex: 1,
    },
    {
      field: 'instanceId',
      headerName: 'Instance ID',
      renderCell: (params) => (
        <a
          href={`${api.defaults.baseURL}/vm/aws/details/${params.value}`}
          style={{ color: '#1976d2', textDecoration: 'none' }}
          target="_blank"
          rel="noopener noreferrer"
        >
          {params.value}
        </a>
      ),
      align: 'left',
      headerAlign: 'center',
      minWidth: 120,
      flex: 1,
    },
    {
      field: 'type',
      headerName: 'Instance Type',
      renderCell: (params) => (
        <Tooltip title={params.value}>
          <Typography variant="body2" noWrap>{params.value}</Typography>
        </Tooltip>
      ),
      align: 'center',
      headerAlign: 'left',
      minWidth: 120,
      flex: 1,
    },
    {
      field: 'zone',
      headerName: 'Zone',
      renderCell: (params) => (
        <Tooltip title={params.value}>
          <Typography variant="body2" noWrap>{params.value}</Typography>
        </Tooltip>
      ),
      align: 'center',
      headerAlign: 'center',
      minWidth: 70,
      flex: 1,
    },
    {
      field: 'publicIPV4Dns',
      headerName: 'Public IPV4 DNS',
      renderCell: (params) => (
        <Tooltip title={params.value}>
          <Typography variant="body2" noWrap>{params.value}</Typography>
        </Tooltip>
      ),
      align: 'center',
      headerAlign: 'center',
      minWidth: 150,
      flex: 1,
    },
    {
      field: 'publicIPV4Address',
      headerName: 'Public IPV4 Address',
      renderCell: (params) => (
        <Tooltip title={params.value}>
          <Typography variant="body2" noWrap>{params.value}</Typography>
        </Tooltip>
      ),
      align: 'center',
      headerAlign: 'center',
      minWidth: 170,
      flex: 1,
    },
    {
      field: 'securityGroup',
      headerName: 'Security Group Name',
      renderCell: (params) => (
        <Tooltip title={params.value}>
          <Typography variant="body2" noWrap>{params.value}</Typography>
        </Tooltip>
      ),
      align: 'center',
      headerAlign: 'center',
      minWidth: 170,
      flex: 1,
    },
    {
      field: 'platform',
      headerName: 'Platform',
      renderCell: (params) => (
        <Tooltip title={params.value || 'N/A'}>
          <Typography variant="body2" noWrap>{params.value || 'N/A'}</Typography>
        </Tooltip>
      ),
      align: 'center',
      headerAlign: 'center',
      minWidth: 60,
      flex: 1,
    },
    {
      field: 'state',
      headerName: 'Instance State',
      renderCell: (params) => (
        <Box display="flex" alignItems="center">
          <FiberManualRecordIcon style={{ color: stateColors[params.value], marginRight: '5px' }} />
          <Typography variant="body2">
            {params.value.charAt(0).toUpperCase() + params.value.slice(1)}
          </Typography>
        </Box>
      ),
      align: 'center',
      headerAlign: 'center',
      minWidth: 120,
      flex: 1,
    },
  ];

  // Prepare rows for DataGrid
  const rows = filteredInstances.map((vm) => ({ id: vm.instanceId, ...vm }));
  console.log("Grid Height", gridHeight);
  // Custom No Rows Overlay
  const AnimatedDots = () => (
    <span style={{ display: 'inline-block', marginLeft: '4px' }}>
      <span style={{ animation: 'dots 1.5s steps(3, end) infinite' }}>...</span>
    </span>
  );
  const CustomLoadingOverlay = () => (
    <Box
      sx={{
        textAlign: 'center',
        p: 3,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        height: '100%',
        backgroundColor: 'rgba(255, 255, 255, 0.8)', // Optional: add semi-transparent background
      }}
    >
      <img src={vmLoading} alt="Cloud and Servers" style={{ width: '400px', height: '400px' }} />
      <Typography variant="h6" gutterBottom style={{ marginTop: '-100px' }}>
       Loading VM's <AnimatedDots />
      </Typography>
      <style>
      {`
        @keyframes dots {
          0%, 20% { color: transparent; }
          40% { color: black; }
          60%, 100% { color: transparent; }
        }
      `}
    </style>
    </Box>
  );

  const CustomNoRowsOverlay = () => {
    return (
      <Box
        sx={{
          textAlign: 'center',
          p: 3,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          height: '100%',
        }}
      >
        <Box
          component="img"
          src={noVmsImage}
          alt="No VMs Found"
          sx={{ width: 200, height: 150, mb: 2 }}
        />
        <Typography variant="h6" gutterBottom>
          No VMs available. Add a cloud account or launch a new instance.
        </Typography>
        <Button
          variant="contained"
          color="primary"
          onClick={() => window.location.href = '/clouds'}
        >
          Add New Cloud Account
        </Button>
      </Box>
    );
  };

  return (
    <Layout>
      <Box display="flex" flexDirection="column" height="100%" sx={{ p: 2 }}>
        <Box
          display="flex"
          justifyContent="space-between"
          alignItems="center"
          mb={2}
          id="grid-header"
        >
          <Typography variant="h6">
            <strong>Instances</strong> ({runningInstances}/{totalInstances})
          </Typography>
          <Box display="flex" alignItems="center">
            <FormControlLabel
              control={
                <Checkbox
                  size="small"
                  checked={showTerminated}
                  onChange={handleShowTerminatedChange}
                />
              }
              label={<Typography variant="body2">Show Terminated Instances</Typography>}
              style={{ margin: '0' }}
            />
            <IconButton color="primary" onClick={handleRefresh} size="small">
              <RefreshIcon fontSize="small" />
            </IconButton>
            <FormControl variant="outlined" size="small" style={{ marginLeft: '10px', minWidth: '180px' }}>
              <InputLabel>Instance Action</InputLabel>
              <Select
                value={selectedAction}
                onChange={(e) => {
                  const action = e.target.value;
                  setSelectedAction(action);
                  handleActionChange(action);
                }}
                label="Instance Action"
                disabled={selectedInstances.length === 0}
              >
                <MenuItem value="start" disabled={hasRunning}>Start Instance</MenuItem>
                <MenuItem value="stop" disabled={hasStopped}>Stop Instance</MenuItem>
                <MenuItem value="reboot" disabled={hasStopped}>Reboot Instance</MenuItem>
                <MenuItem value="hibernate" disabled={hasStopped}>Hibernate Instance</MenuItem>
                <MenuItem value="terminate" disabled={hasRunning}>Terminate Instance</MenuItem>
              </Select>
            </FormControl>
            <Button
              variant="contained"
              color="primary"
              onClick={() => console.log('Launching Instances')}
              style={{ marginLeft: '10px' }}
            >
              Launch Instances
            </Button>
          </Box>
        </Box>

        <Box sx={{ flexGrow: 1, height: '71vh' }}>
          <DataGrid
            rows={rows}
            columns={columns}
            pageSize={10}
            rowsPerPageOptions={[10, 25, 50, 100]}
            disableSelectionOnClick
            checkboxSelection={false}
            loading={loading}
            slots={{
              noRowsOverlay: CustomNoRowsOverlay,
              loadingOverlay: CustomLoadingOverlay,
            }}
            sx={{
              border: '1px solid rgba(224, 224, 224, 1)', // Outline border
              '& .MuiDataGrid-cell': {
                borderBottom: 'none', // Remove cell borders
                padding: '8px',
                alignItems: 'center',
                display: 'flex',
              },
              '& .MuiDataGrid-columnHeaders': {
                backgroundColor: '#f5f5f5',
                borderBottom: '1px solid rgba(224, 224, 224, 1)', // Subtle bottom border
                alignItems: 'center',
              },
              '& .MuiDataGrid-columnHeaderTitle': {
                fontWeight: 'bold',
              },
              '& .MuiDataGrid-row:hover': {
                backgroundColor: '#f9f9f9', // Lighter hover effect
              },
              '& .MuiDataGrid-cellContent': {
                display: 'flex',
                alignItems: 'center',
              },
              '& .MuiDataGrid-footerContainer': {
                borderTop: '1px solid rgba(224, 224, 224, 1)', // Top border for footer
              },
              '& .MuiDataGrid-virtualScrollerContent': {
                minHeight: 'unset !important',
              },
              '& .MuiDataGrid-row': {
                maxHeight: 'none !important',
              },
            }}
          />
        </Box>
      </Box>
    </Layout>
  );
};

export default VMs;