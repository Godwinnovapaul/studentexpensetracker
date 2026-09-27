package com.expense.expensetracker.controller;
import com.expense.expensetracker.entity.StudentEntity;
import com.expense.expensetracker.service.StudentExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ExpenseTrackerController {
    private final StudentExpenseService service;
    public ExpenseTrackerController(StudentExpenseService service) {
        this.service = service;
    }
    @PostMapping("/expenses")
    public ResponseEntity<StudentEntity> addExpense(@RequestBody StudentEntity expense) {
        return ResponseEntity.ok(service.addExpense(expense));
    }
    @GetMapping("/expenses")
    public ResponseEntity<List<StudentEntity>> getAllExpenses() {
        return ResponseEntity.ok(service.getAllExpenses());
    }
    @GetMapping("/expenses/{id}")
    public ResponseEntity<StudentEntity> getExpenseById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getExpenseById(id));
    }
    @PutMapping("/expenses/{id}")
    public ResponseEntity<StudentEntity> updateExpense(@PathVariable Long id, @RequestBody StudentEntity expense) {
        return ResponseEntity.ok(service.updateExpense(id, expense));
    }
    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<Map<String, String>> deleteExpense(@PathVariable Long id) {
        service.deleteExpense(id);
        return ResponseEntity.ok(Map.of("message", "Expense deleted successfully with id: " + id));
    }
    @PostMapping("/budgets")
    public ResponseEntity<StudentEntity> setBudget(@RequestBody Map<String, Object> payload) {
        String category = (String) payload.get("category");
        Double limit = Double.valueOf(payload.get("amount").toString());
        return ResponseEntity.ok(service.setBudget(category, limit));
    }
    @GetMapping("/budgets")
    public ResponseEntity<List<StudentEntity>> getAllBudgets() {
        return ResponseEntity.ok(service.getAllBudgets());
    }
    @GetMapping("/summary")
    public ResponseEntity<List<Map<String, Object>>> getSummary() {
        return ResponseEntity.ok(service.getCurrentMonthSummary());
    }
    @GetMapping("/trends")
    public ResponseEntity<List<Map<String, Object>>> getTrends() {
        return ResponseEntity.ok(service.getMonthlyTrends());
    }
}
