package com.example.telitodev.repository;

import com.example.telitodev.entity.VersionApi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VersionApiRepository extends JpaRepository<VersionApi, Integer> {

}
