package com.example.telitodev.repository;

import com.example.telitodev.entity.ApiHasEntorno;
import com.example.telitodev.entity.ApiHasEntornoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApiHasEntornoRepository extends JpaRepository<ApiHasEntorno, ApiHasEntornoId> {


    List<ApiHasEntorno> findByApi_IdApi(Integer apiId);

}