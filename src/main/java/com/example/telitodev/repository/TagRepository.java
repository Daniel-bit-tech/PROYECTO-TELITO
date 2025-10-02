package com.example.telitodev.repository;

import com.example.telitodev.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {
    //esta tambien funciona tanto para DEV-PO
}
