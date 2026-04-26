package com.csdlpt.web.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.csdlpt.web.entity.AppUser;
import org.springframework.data.jpa.repository.Query;

public interface AppUserRepository extends JpaRepository<AppUser, String> {

    @EntityGraph(attributePaths = "station")
    Optional<AppUser> findByUsername(String username);

    List<AppUser> findByStationId(String stationId);

    @Query("""
    SELECT u FROM AppUser u
    WHERE (:stationId IS NULL OR :stationId = '' OR u.station.id = :stationId)
    AND (
        :keyword IS NULL OR :keyword = '' OR
        LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
        LOWER(u.fullname) LIKE LOWER(CONCAT('%', :keyword, '%'))
    )
""")
    List<AppUser> search(String stationId, String keyword);
}

