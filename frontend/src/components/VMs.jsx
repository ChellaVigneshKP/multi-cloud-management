import React, { useState, useEffect } from 'react';
import {
  Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Button, Select, MenuItem, IconButton, FormControl, InputLabel,
  Checkbox, FormControlLabel, Box
} from '@mui/material';
import RefreshIcon from '@mui/icons-material/Refresh';
import FiberManualRecordIcon from '@mui/icons-material/FiberManualRecord';
import Layout from './Layout';
import Cookies from 'js-cookie';
import api from '../api';

const VMs = () => {
  const [instances, setInstances] = useState([]);
  const [selectedInstances, setSelectedInstances] = useState([]);
  const [showTerminated, setShowTerminated] = useState(false);
  const [selectedAction, setSelectedAction] = useState('');

  const fetchInstances = async () => {
    const apiToken = Cookies.get('apiToken');
    if (!apiToken) {
      console.error('API Token is missing');
      return;
    }
    try {
      const response = await fetch(`${api.defaults.baseURL}/vm/aws/ec2`, {
        headers: {
          'Authorization': `Bearer ${apiToken}`,
        },
      });
      if (!response.ok) {
        throw new Error('Failed to fetch instances');
      }
      const data = await response.json();
      setInstances(data.instances); // Adjust based on API response structure
    } catch (error) {
      console.error('Error fetching instances:', error);
    }
  };

  useEffect(() => {
    fetchInstances();
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

  const handleCheckboxChange = (instanceId) => {
    setSelectedInstances((prevSelected) =>
      prevSelected.includes(instanceId)
        ? prevSelected.filter(id => id !== instanceId)
        : [...prevSelected, instanceId]
    );
  };

  const filteredInstances = instances.filter(instance => {
    if (!showTerminated && instance.state === 'terminated') return false;
    return true;
  });

  const totalInstances = instances.length;
  const runningInstances = instances.filter(instance => instance.state === 'running').length;

  const stateColors = {
    running: '#00e676',
    stopped: '#ff5252',
    terminated: '#bdbdbd',
    starting: '#ffeb3b',
    stopping: '#ffeb3b',
  };

  const hasRunning = selectedInstances.some(id => instances.find(inst => inst.instanceId === id).state === 'running');
  const hasStopped = selectedInstances.some(id => instances.find(inst => inst.instanceId === id).state === 'stopped');

  return (
    <Layout>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <div style={{ fontSize: '0.9rem' }}>
          <strong>Instances</strong> ({runningInstances}/{totalInstances})
        </div>
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <FormControlLabel
            control={<Checkbox size="small" checked={showTerminated} onChange={handleShowTerminatedChange} />}
            label={<span style={{ fontSize: '0.8rem' }}>Show Terminated Instances</span>}
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
              style={{ fontSize: '0.8rem' }}
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
            style={{ marginLeft: '10px', fontSize: '0.8rem' }}
          >
            Launch Instances
          </Button>
        </div>
      </div>

      <TableContainer>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell></TableCell>
              <TableCell style={{ width: '120px' }}><strong>Provider</strong></TableCell>
              <TableCell style={{ width: '100px' }}><strong>KeyID</strong></TableCell>
              <TableCell style={{ width: '150px' }}><strong>Instance Name</strong></TableCell>
              <TableCell style={{ width: '150px' }}><strong>Instance ID</strong></TableCell>
              <TableCell style={{ width: '120px' }}><strong>Instance Type</strong></TableCell>
              <TableCell style={{ width: '120px' }}><strong>Zone</strong></TableCell>
              <TableCell style={{ width: '200px' }}><strong>Public IPV4 DNS</strong></TableCell>
              <TableCell style={{ width: '150px' }}><strong>Public IPV4 Address</strong></TableCell>
              <TableCell style={{ width: '150px' }}><strong>Security Group Name</strong></TableCell>
              <TableCell style={{ width: '100px' }}><strong>Platform</strong></TableCell>
              <TableCell style={{ width: '150px' }}><strong>Instance State</strong></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filteredInstances.map((vm) => (
              <TableRow key={vm.instanceId}>
                <TableCell>
                  <Checkbox
                    checked={selectedInstances.includes(vm.instanceId)}
                    onChange={() => handleCheckboxChange(vm.instanceId)}
                    size="small"
                  />
                </TableCell>
                <TableCell title={vm.provider} style={{ maxWidth: '120px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{vm.provider}</TableCell>
                <TableCell title={vm.keyId} style={{ maxWidth: '100px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{vm.keyId}</TableCell>
                <TableCell title={vm.name} style={{ maxWidth: '150px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{vm.name}</TableCell>
                <TableCell style={{ whiteSpace: 'nowrap', overflow: 'visible', textOverflow: 'clip' }}>
                  <a
                    href={`${api.defaults.baseURL}/vm/aws/details/${vm.instanceId}`} // Update URL as needed
                    style={{ color: 'blue', textDecoration: 'underline' }}
                    target="_blank" // Optional: opens the link in a new tab
                    rel="noopener noreferrer" // Optional: for security
                  >
                    {vm.instanceId}
                  </a>
                </TableCell>
                <TableCell title={vm.type} style={{ maxWidth: '120px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{vm.type}</TableCell>
                <TableCell title={vm.zone} style={{ maxWidth: '120px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{vm.zone}</TableCell>
                <TableCell title={vm.publicIPV4Dns} style={{ maxWidth: '200px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{vm.publicIPV4Dns}</TableCell>
                <TableCell title={vm.publicIPV4Address} style={{ maxWidth: '150px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{vm.publicIPV4Address}</TableCell>
                <TableCell title={vm.securityGroup} style={{ maxWidth: '150px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{vm.securityGroup}</TableCell>
                <TableCell title={vm.platform} style={{ maxWidth: '100px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{vm.platform}</TableCell>
                <TableCell>
                  <Box display="flex" alignItems="center">
                    <FiberManualRecordIcon style={{ color: stateColors[vm.state], marginRight: '5px' }} />
                    {vm.state.charAt(0).toUpperCase() + vm.state.slice(1)}
                  </Box>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Layout>
  );
};

export default VMs;
