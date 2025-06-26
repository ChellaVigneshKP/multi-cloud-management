"use client";
import PropTypes from 'prop-types';
import Button from '@mui/material/Button';
import CircularProgress from '@mui/material/CircularProgress';
import { styled } from '@mui/material/styles';

const StyledButton = styled(Button)(({ theme }) => ({
  position: 'relative',
}));

const LoadingButton = ({ loading = false, children, ...props }) => (
  <StyledButton {...props} disabled={loading || props.disabled}>
    {loading && (
      <CircularProgress
        size={24}
        sx={{
          color: 'primary.contrastText',
          position: 'absolute',
          top: '50%',
          left: '50%',
          marginTop: '-12px',
          marginLeft: '-12px',
        }}
        aria-label="Loading"
      />
    )}
    {children}
  </StyledButton>
);

LoadingButton.propTypes = {
  loading: PropTypes.bool,
  children: PropTypes.node.isRequired,
  disabled: PropTypes.bool,
};

export default LoadingButton;
