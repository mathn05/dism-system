package com.csdlpt.web.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.csdlpt.web.service.AppUserDetailsService;

@Component
public class StationAuthenticationProvider extends DaoAuthenticationProvider {

    public StationAuthenticationProvider(AppUserDetailsService appUserDetailsService,
                                         PasswordEncoder passwordEncoder) {
        super(appUserDetailsService);
        setPasswordEncoder(passwordEncoder);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication) {
        super.additionalAuthenticationChecks(userDetails, authentication);

        if (!(authentication.getDetails() instanceof StationWebAuthenticationDetails details)) {
            throw new BadCredentialsException("Invalid station");
        }

        if (!(userDetails instanceof AppUserPrincipal principal)) {
            throw new BadCredentialsException("Invalid user");
        }

        String stationId = details.getStationId();
        if (stationId == null || stationId.isBlank()) {
            throw new BadCredentialsException("Station is required");
        }

        if (!principal.getStationId().equalsIgnoreCase(stationId.trim())) {
            throw new BadCredentialsException("Invalid station");
        }
    }
}


