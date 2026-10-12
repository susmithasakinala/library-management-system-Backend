package com.example.libapp.repository;

import com.example.libapp.entity.IssueRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IssueRecordRepository extends JpaRepository<IssueRecord, Long> {
    List<IssueRecord> findByStatus(String status);

    // how many books a student currently holds (status = ISSUED)
    long countByStudentIdAndStatus(Long studentId, String status);
}