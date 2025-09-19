package com.example.telitodev.service;

import com.example.telitodev.entity.Api;
import com.example.telitodev.repository.ApiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ApiService {

    @Autowired
    private ApiRepository apiRepository;

    public List<Api> getRecentApis() {
        List<Api> allApis = apiRepository.findAll();

        return allApis.stream()
                .sorted(Comparator.comparing(Api::getFechaCreacion).reversed())
                .limit(3)
                .collect(Collectors.toList());
    }


    public Optional<Api> getApiById(Integer id) {
        return apiRepository.findById(id);
    }


    public List<Api> getAllApis() {
        return apiRepository.findAll();
    }
}