package com.csdlpt.web.security;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.stereotype.Component;

@Component
public class StationWebAuthenticationDetailsSource
    implements AuthenticationDetailsSource<HttpServletRequest, StationWebAuthenticationDetails> {

    @Override
    public StationWebAuthenticationDetails buildDetails(HttpServletRequest context) {
        return new StationWebAuthenticationDetails(context);
    }
}

