import React, { useState, useEffect } from 'react';
import axios from 'axios';
import Cookies from 'js-cookie';
import { Box, Button, TextField, MenuItem, Typography, Paper, useMediaQuery } from '@mui/material';
import { useTheme } from '@mui/material/styles';
import Layout from './Layout';

// Function to fetch logos
const getCloudLogo = (provider) => {
  switch (provider) {
    case 'AWS':
      return '/images/aws-logo.png'; // Replace with actual AWS logo URL
    case 'GCP':
      return '/images/gcp-logo.png'; // Replace with actual GCP logo URL
    case 'Azure':
      return '/images/azure-logo.png'; // Replace with actual Azure logo URL
    default:
      return '';
  }
};

const Clouds = () => {
  const [cloudAccounts, setCloudAccounts] = useState([]);
  const [cloudProvider, setCloudProvider] = useState('');
  const [awsConfig, setAwsConfig] = useState({ accessKeyId: '', secretAccessKey: '', region: '' });
  const [showForm, setShowForm] = useState(false);
  const [regions, setRegions] = useState([]);
  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const theme = useTheme();
  const isSmallScreen = useMediaQuery(theme.breakpoints.down('sm'));

  // Function to fetch cloud accounts
  const fetchCloudAccounts = async () => {
    const apiToken = Cookies.get('apiToken');
    if (!apiToken) {
      console.error('API Token is missing');
      return;
    }
    try {
      const response = await axios.get('http://localhost:6061/vm/cloudaccounts', {
        headers: {
          'Authorization': `Bearer ${apiToken}`,
        },
      });
      setCloudAccounts(response.data);
    } catch (error) {
      console.error('Error fetching cloud accounts:', error);
    }
  };

  const fetchAwsRegions = async () => {
    const apiToken = Cookies.get('apiToken');
    if (!apiToken) {
      console.error('API Token is missing');
      return;
    }
    try {
      const response = await axios.get('http://localhost:6061/vm/aws/regions', {
        headers: {
          Authorization: `Bearer ${apiToken}`,
        },
      });
      // const response = await axios.get('http://localhost:6061/vm/aws/regions');
      setRegions(response.data.regions);
    } catch (error) {
      console.error('Error fetching AWS regions:', error);
    }
  };
  // Fetch cloud accounts when component mounts
  useEffect(() => {
    fetchCloudAccounts();
  }, []);

  useEffect(() => {
    if (cloudProvider === 'aws') {
      fetchAwsRegions();
    }
  }, [cloudProvider]);

  // Handle changes in AWS config fields
  const handleAWSConfigChange = (event) => {
    setAwsConfig({ ...awsConfig, [event.target.name]: event.target.value });
  };

  // Handle form submission for adding AWS account
  const handleSubmit = async () => {
    let endpoint = '';
    const awsData = {
      access_key_id: awsConfig.accessKeyId,
      secret_access_key: awsConfig.secretAccessKey,
      region: awsConfig.region,
    };

    try {
      const apiToken = Cookies.get('apiToken');
      if (!apiToken) {
        console.error('API Token is missing');
        return;
      }
      if (cloudProvider === 'aws') {
        endpoint = 'http://localhost:6061/vm/aws/addaccount';
      } else if (cloudProvider === 'gcp') {
        endpoint = 'http://localhost:6061/vm/gcp/addaccount';
      } else if (cloudProvider === 'azure') {
        endpoint = 'http://localhost:6061/vm/azure/addaccount';
      }
      const response = await axios.post(endpoint, awsData, {
        headers: {
          Authorization: `Bearer ${apiToken}`,
          'Content-Type': 'application/json',
        },
      });
      console.log(response);

      setSuccessMessage(`${cloudProvider.toUpperCase()} cloud account added successfully`);
      setErrorMessage(''); // Clear error message
      setAwsConfig({ accessKeyId: '', secretAccessKey: '', region: '' }); // Clear form fields
      setShowForm(false); // Close the form
      fetchCloudAccounts(); // Refresh the cloud accounts list
    } catch (error) {
      setErrorMessage(error.response?.data?.message || `Error adding ${cloudProvider.toUpperCase()} cloud account`);
      setSuccessMessage(''); // Clear success message
    }
  };

  const handleAddClick = () => {
    setShowForm(true);
    setErrorMessage(''); // Reset error message
    setSuccessMessage(''); // Reset success message
  };

  return (
    <Layout>
      {!showForm ? (
        <Box
          sx={{
            display: 'flex',
            flexWrap: 'wrap',
            justifyContent: isSmallScreen ? 'center' : 'flex-start',
            gap: 2,
            mt: 4,
          }}
        >
          {/* Display existing cloud accounts */}
          {cloudAccounts.length > 0 ? (
            cloudAccounts.map((account, index) => (
              <Paper
                key={index}
                sx={{
                  border: '1px solid #ccc',
                  borderRadius: 2,
                  width: isSmallScreen ? '45%' : '22%',
                  p: 2,
                  textAlign: 'center',
                  wordWrap: 'break-word',
                }}
              >
                <img src={getCloudLogo(account.cloud_name)} alt={account.cloud_name} style={{ height: 50 }} />
                <Typography variant="h6" gutterBottom>
                  {account.cloud_name}
                </Typography>
                {account.access_key_id && (
                  <Typography sx={{ fontWeight: 'bold' }}>Access Key ID: {account.access_key_id}</Typography>
                )}
                {account.project_id && <Typography>Project ID: {account.project_id}</Typography>}
                {account.client_id && <Typography>Client ID: {account.client_id}</Typography>}
              </Paper>
            ))
          ) : (
            <Box sx={{ display: 'flex', justifyContent: 'center', width: '100%' }}>
              <Box
                sx={{
                  border: '2px dashed #ccc',
                  borderRadius: 2,
                  width: '70%',
                  height: '200px',
                  display: 'flex',
                  flexDirection: 'column',
                  justifyContent: 'center',
                  alignItems: 'center',
                  textAlign: 'center',
                }}
              >
                <Typography variant="h6" gutterBottom>
                  No cloud accounts added.
                </Typography>
                <Button variant="contained" color="primary" onClick={handleAddClick} sx={{ mt: 2 }}>
                  Add Account
                </Button>
              </Box>
            </Box>
          )}

          {/* Add Account Box */}
          {cloudAccounts.length > 0 && (
            <Box
              sx={{
                border: '2px dashed #ccc',
                borderRadius: 2,
                width: isSmallScreen ? '45%' : '22%',
                height: '200px',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'center',
                alignItems: 'center',
                textAlign: 'center',
                transition: 'background-color 0.3s, box-shadow 0.3s',
                cursor: 'pointer',
                '&:hover': {
                  backgroundColor: '#f0f0f0', // Light gray background on hover
                  boxShadow: '0px 4px 12px rgba(0, 0, 0, 0.1)', // Add shadow on hover
                },
              }}
              onClick={handleAddClick}
            >
              <Typography variant="h6">+ Add Cloud Account</Typography>
            </Box>
          )}
        </Box>
      ) : (
        <Paper sx={{ p: 4, maxWidth: 600, mx: 'auto' }}>
          <Typography variant="h4" gutterBottom>
            Add Cloud Account
          </Typography>

          <TextField
            select
            label="Select Cloud Provider"
            value={cloudProvider}
            onChange={(e) => setCloudProvider(e.target.value)}
            fullWidth
            margin="normal"
          >
            <MenuItem value="aws">Amazon AWS</MenuItem>
            <MenuItem value="gcp">Google Cloud Platform</MenuItem>
            <MenuItem value="azure">Microsoft Azure</MenuItem>
          </TextField>

          {/* AWS Fields */}
          {cloudProvider === 'aws' && (
            <Box sx={{ mt: 2 }}>
              <Typography variant="h6">AWS Configuration</Typography>
              <TextField
                label="Access Key ID"
                name="accessKeyId"
                value={awsConfig.accessKeyId}
                onChange={handleAWSConfigChange}
                fullWidth
                margin="normal"
              />
              <TextField
                label="Secret Access Key"
                name="secretAccessKey"
                value={awsConfig.secretAccessKey}
                onChange={handleAWSConfigChange}
                fullWidth
                margin="normal"
                type="password"
              />
              <TextField select label="Region" name="region" value={awsConfig.region} onChange={handleAWSConfigChange} fullWidth margin="normal">
                {regions.map((region) => (
                  <MenuItem key={region} value={region}>{region}</MenuItem>
                ))}
              </TextField>
            </Box>
          )}

          {/* Error Message */}
          {errorMessage && (
            <Typography color="error" sx={{ mt: 2 }}>
              {errorMessage}
            </Typography>
          )}

          {/* Success Message */}
          {successMessage && (
            <Typography color="primary" sx={{ mt: 2 }}>
              {successMessage}
            </Typography>
          )}

          <Button variant="contained" color="primary" fullWidth sx={{ mt: 4 }} onClick={handleSubmit}>
            Add Cloud Account
          </Button>
        </Paper>
      )}
    </Layout>
  );
};

export default Clouds;
