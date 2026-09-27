package com.expense.expensetracker.service;
import com.expense.expensetracker.entity.StudentEntity;
import com.expense.expensetracker.repository.StudentRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
@Service
public class StudentExpenseService {
    private final StudentRepository repository;
    public StudentExpenseService(StudentRepository repository) {
        this.repository = repository;
    }
    public StudentEntity addExpense(StudentEntity expense) {
        expense.setRecordType("EXPENSE");
        if (expense.getDate() == null) {
            expense.setDate(LocalDate.now());
        }
        return repository.save(expense);
    }
    public List<StudentEntity> getAllExpenses() {
        return repository.findByRecordType("EXPENSE");
    }
    public StudentEntity getExpenseById(Long id) {
        return repository.findById(id)
                .filter(e -> "EXPENSE".equalsIgnoreCase(e.getRecordType()))
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));
    }
    public StudentEntity updateExpense(Long id, StudentEntity updatedExpense) {
        return repository.findById(id).map(existing -> {
            existing.setTitle(updatedExpense.getTitle());
            existing.setAmount(updatedExpense.getAmount());
            existing.setCategory(updatedExpense.getCategory());
            if (updatedExpense.getDate() != null) {
                existing.setDate(updatedExpense.getDate());
            }
            return repository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));
    }
    public void deleteExpense(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Expense not found with id: " + id);
        }
        repository.deleteById(id);
    }
    public StudentEntity setBudget(String category, Double limit) {
        Optional<StudentEntity> existing = repository.findByRecordTypeAndCategoryIgnoreCase("BUDGET", category);
        StudentEntity budget = existing.orElseGet(StudentEntity::new);
        budget.setRecordType("BUDGET");
        budget.setCategory(category);
        budget.setAmount(limit);
        return repository.save(budget);
    }
    public List<StudentEntity> getAllBudgets() {
        return repository.findByRecordType("BUDGET");
    }
    public List<Map<String, Object>> getCurrentMonthSummary() {
        YearMonth currentMonth = YearMonth.now();
        LocalDate start = currentMonth.atDay(1);
        LocalDate end = currentMonth.atEndOfMonth();
        List<StudentEntity> expenses = repository.findByRecordTypeAndDateBetween("EXPENSE", start, end);
        List<StudentEntity> budgets = repository.findByRecordType("BUDGET");
        Map<String, Double> spendByCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        StudentEntity::getCategory,
                        Collectors.summingDouble(StudentEntity::getAmount)
                ));
        Set<String> allCategories = new HashSet<>(spendByCategory.keySet());
        budgets.forEach(b -> allCategories.add(b.getCategory()));
        List<Map<String, Object>> summaries = new ArrayList<>();
        for (String cat : allCategories) {
            double spent = spendByCategory.getOrDefault(cat, 0.0);
            double limit = budgets.stream()
                    .filter(b -> b.getCategory().equalsIgnoreCase(cat))
                    .map(StudentEntity::getAmount)
                    .findFirst()
                    .orElse(0.0);

            double percentage = limit > 0 ? (spent / limit) * 100 : 0.0;
            boolean alert = limit > 0 && percentage >= 90.0;

            Map<String, Object> summary = new HashMap<>();
            summary.put("category", cat);
            summary.put("spent", spent);
            summary.put("budget", limit);
            summary.put("percentage", Math.round(percentage * 100.0) / 100.0);
            summary.put("alert", alert);
            if (alert) {
                summary.put("alertMessage", "Warning: Spend in '" + cat + "' is at " + String.format("%.1f", percentage) + "% of budget!");
            }
            summaries.add(summary);
        }
        return summaries;
    }
    public List<Map<String, Object>> getMonthlyTrends() {
        YearMonth current = YearMonth.now();
        List<Map<String, Object>> trends = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM yyyy");
        for (int i = 5; i >= 0; i--) {
            YearMonth targetMonth = current.minusMonths(i);
            LocalDate start = targetMonth.atDay(1);
            LocalDate end = targetMonth.atEndOfMonth();

            double total = repository.findByRecordTypeAndDateBetween("EXPENSE", start, end)
                    .stream()
                    .mapToDouble(StudentEntity::getAmount)
                    .sum();

            Map<String, Object> trend = new HashMap<>();
            trend.put("month", targetMonth.format(formatter));
            trend.put("total", total);
            trends.add(trend);
        }
        return trends;
    }
}
