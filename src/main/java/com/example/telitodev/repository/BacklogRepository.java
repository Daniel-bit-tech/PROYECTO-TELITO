package com.example.telitodev.repository;

import com.example.telitodev.entity.Backlog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface BacklogRepository extends JpaRepository<Backlog, Integer> {
    Optional<Backlog> findByFeedback_IdFeedback(Integer idFeedback);

}
