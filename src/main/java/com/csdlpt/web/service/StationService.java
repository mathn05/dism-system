package com.csdlpt.web.service;

import com.csdlpt.web.entity.Station;
import com.csdlpt.web.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StationService {

    private final StationRepository stationRepository;

    public List<Station> findAllExceptHeadquarter() {
        return stationRepository.findAllExceptHeadquarter();
    }

    public Station findById(String id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Station not found: " + id));
    }

    public void create(Station station) {
        if (station.getHeadquarter() == null) {
            station.setHeadquarter(false);
        }
        if (stationRepository.existsById(station.getId())) {
            throw new RuntimeException("Station ID already exists");
        }
        stationRepository.save(station);
    }

    public void update(Station station) {
        Station existing = findById(station.getId());

        existing.setName(station.getName());
        existing.setAddress(station.getAddress());
        existing.setHeadquarter(station.getHeadquarter());

        stationRepository.save(existing);
    }

    public void delete(String id) {
        stationRepository.deleteById(id);
    }

    public List<Station> search(String keyword) {
        if (keyword == null) keyword = "";
        return stationRepository.search(keyword);
    }
}