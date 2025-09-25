package com.example.telitodev.repository;

import com.example.telitodev.entity.Issue;
import com.example.telitodev.entity.IssueId;
import com.example.telitodev.entity.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IssueRepository extends JpaRepository<Issue, IssueId> {
}