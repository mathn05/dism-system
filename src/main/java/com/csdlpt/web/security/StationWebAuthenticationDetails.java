package com.csdlpt.web.security;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

public class StationWebAuthenticationDetails extends WebAuthenticationDetails {

    private final String stationId;

    public StationWebAuthenticationDetails(HttpServletRequest request) {
        super(request);
        this.stationId = request.getParameter("stationId");
    }

    public String getStationId() {
        return stationId;
    }
}

