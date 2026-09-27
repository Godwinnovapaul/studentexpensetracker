package com.expense.expensetracker.repository;

import com.expense.expensetracker.entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface StudentRepository extends JpaRepository<StudentEntity, Long> {

    
    List<StudentEntity> findByRecordType(String recordType);

    
    List<StudentEntity> findByRecordTypeAndDateBetween(String recordType, LocalDate startDate, LocalDate endDate);

    
    Optional<StudentEntity> findByRecordTypeAndCategoryIgnoreCase(String recordType, String category);
}
