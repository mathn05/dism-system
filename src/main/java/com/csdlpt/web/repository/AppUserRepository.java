package com.csdlpt.web.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.csdlpt.web.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, String> {

    @EntityGraph(attributePaths = "station")
    Optional<AppUser> findByUsername(String username);
}

