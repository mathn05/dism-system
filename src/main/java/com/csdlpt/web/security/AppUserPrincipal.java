package com.csdlpt.web.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.csdlpt.web.entity.AppUser;

public class AppUserPrincipal implements UserDetails {

    private final String id;
    private final String username;
    private final String password;
    private final String fullName;
    private final String stationId;
    private final List<GrantedAuthority> authorities;

    public AppUserPrincipal(AppUser appUser) {
        this.id = appUser.getId();
        this.username = appUser.getUsername();
        this.password = appUser.getPassword();
        this.fullName = appUser.getFullname();
        this.stationId = appUser.getStation().getId();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + appUser.getRole().name()));
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getStationId() {
        return stationId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

