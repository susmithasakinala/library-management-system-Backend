package com.example.libapp.controller;

import com.example.libapp.entity.IssueRecord;
import com.example.libapp.service.IssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/issues")
public class IssueController {

    @Autowired
    private IssueService issueService;

    @GetMapping
    public List<IssueRecord> getAll(@RequestParam(required = false) String status) {
        if ("ISSUED".equals(status)) return issueService.getIssuedOnly();
        return issueService.getAllIssues();
    }

    @GetMapping("/overdue")
    public List<Map<String, Object>> getOverdue() {
        return issueService.getOverdueList();
    }

    @GetMapping("/total-fine")
    public Map<String, Object> getTotalFine() {
        return Map.of("totalFine", issueService.getTotalFinesCollected());
    }

    @PostMapping
    public IssueRecord issueBook(@RequestBody Map<String, Long> body) {
        return issueService.issueBook(body.get("studentId"), body.get("bookId"));
    }

    @PutMapping("/{id}/overdue")
    public IssueRecord markOverdue(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        return issueService.markOverdue(id, body.get("days"));
    }

    @PutMapping("/{id}/return")
    public IssueRecord returnBook(@PathVariable Long id) {
        return issueService.returnBook(id);
    }
}