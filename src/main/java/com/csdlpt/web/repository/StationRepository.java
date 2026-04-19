package com.csdlpt.web.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.csdlpt.web.entity.Station;

public interface StationRepository extends JpaRepository<Station, String> {

    List<Station> findAllByOrderByIdAsc();

    List<Station> findByHeadquarterTrueOrderByIdAsc();
}

