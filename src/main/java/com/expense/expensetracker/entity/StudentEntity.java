package com.expense.expensetracker.entity;
import jakarta.persistence.*;
import java.time.LocalDate;
@Entity
@Table(name = "student_records")
public class StudentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String recordType;
    private String title;
    private Double amount;
    private String category;
    private LocalDate date;
    public StudentEntity() {}
    public StudentEntity(String recordType, String title, Double amount, String category, LocalDate date) {
        this.recordType = recordType;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.date = date;
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRecordType() { return recordType; }
    public void setRecordType(String recordType) { this.recordType = recordType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
