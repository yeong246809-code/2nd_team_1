package org.example.k_market.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.example.k_market.dao.VersionDAO;
import org.example.k_market.dto.VersionDTO;
import org.example.k_market.entity.Version;
import org.example.k_market.repository.VersionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
@Service
public class VersionService {
    private final VersionDAO dao;
    private final VersionRepository repository;

    public List<VersionDTO> getAll(){
        List<Version> entityList = repository.findAll();
        List<VersionDTO> dtoList = entityList.stream()
                .map(entity -> entity.toDTO())
                .toList();
        return dtoList;
    }
}
