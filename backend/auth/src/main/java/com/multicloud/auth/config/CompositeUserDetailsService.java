package com.multicloud.auth.config;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

public class CompositeUserDetailsService implements UserDetailsService {

    private final List<UserDetailsService> userDetailsServices;

    public CompositeUserDetailsService(List<UserDetailsService> userDetailsServices) {
        this.userDetailsServices = userDetailsServices;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        for (UserDetailsService uds : userDetailsServices) {
            try {
                return uds.loadUserByUsername(username);
            } catch (UsernameNotFoundException e) {
                // Continue to next UserDetailsService
            }
        }
        throw new UsernameNotFoundException("User not found: " + username);
    }
}
