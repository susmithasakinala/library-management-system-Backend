package com.example.libapp.service;

import com.example.libapp.entity.Book;
import com.example.libapp.entity.IssueRecord;
import com.example.libapp.entity.Student;
import com.example.libapp.repository.BookRepository;
import com.example.libapp.repository.IssueRecordRepository;
import com.example.libapp.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IssueService {

    @Autowired
    private IssueRecordRepository issueRecordRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private StudentRepository studentRepository;

    private static final int LOAN_DAYS = 14;
    private static final int MAX_BOOKS_PER_STUDENT = 3;

    // The library's fine rules (change the amounts here if needed)
    // 1-3 days late = Rs.10, 4-7 days = Rs.20, 8-14 days = Rs.50, 15+ days = Rs.100
    public double calculateFine(long lateDays) {
        if (lateDays <= 0) return 0;
        if (lateDays <= 3) return 10;
        if (lateDays <= 7) return 20;
        if (lateDays <= 14) return 50;
        return 100;
    }

    public List<IssueRecord> getAllIssues() {
        return issueRecordRepository.findAll();
    }

    public List<IssueRecord> getIssuedOnly() {
        return issueRecordRepository.findByStatus("ISSUED");
    }

    // Books that are still with the student AND past the due date
    public List<Map<String, Object>> getOverdueList() {
        LocalDate today = LocalDate.now();
        List<Map<String, Object>> result = new ArrayList<>();

        for (IssueRecord r : issueRecordRepository.findByStatus("ISSUED")) {
            long lateDays = ChronoUnit.DAYS.between(r.getDueDate(), today);
            if (lateDays > 0) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", r.getId());
                row.put("studentName", r.getStudent().getName());
                row.put("rollNo", r.getStudent().getRollNo());
                row.put("bookTitle", r.getBook().getTitle());
                row.put("dueDate", r.getDueDate().toString());
                row.put("daysLate", lateDays);
                row.put("fine", calculateFine(lateDays));
                result.add(row);
            }
        }
        return result;
    }

    // Sum of all fines from books that were already returned
    public double getTotalFinesCollected() {
        double total = 0;
        for (IssueRecord r : issueRecordRepository.findByStatus("RETURNED")) {
            total += r.getFineAmount();
        }
        return total;
    }

    public IssueRecord issueBook(Long studentId, Long bookId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        // rule: a student can hold at most 3 books at a time
        long currentlyHeld = issueRecordRepository.countByStudentIdAndStatus(studentId, "ISSUED");
        if (currentlyHeld >= MAX_BOOKS_PER_STUDENT) {
            throw new RuntimeException("Limit reached: " + student.getName()
                    + " already has " + MAX_BOOKS_PER_STUDENT + " books issued");
        }

        if (book.getAvailable() <= 0) {
            throw new RuntimeException("Book is not available");
        }

        IssueRecord record = new IssueRecord();
        record.setStudent(student);
        record.setBook(book);
        record.setIssueDate(LocalDate.now());
        record.setDueDate(LocalDate.now().plusDays(LOAN_DAYS));
        record.setStatus("ISSUED");
        record.setFineAmount(0);

        book.setAvailable(book.getAvailable() - 1);
        bookRepository.save(book);

        return issueRecordRepository.save(record);
    }

    // Marks an issued book as overdue by moving its due date back
    public IssueRecord markOverdue(Long issueId, Integer days) {
        IssueRecord record = issueRecordRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue record not found"));

        if ("RETURNED".equals(record.getStatus())) {
            throw new RuntimeException("Book already returned");
        }
        if (days == null || days < 1) {
            throw new RuntimeException("Days overdue must be at least 1");
        }

        record.setDueDate(LocalDate.now().minusDays(days));
        return issueRecordRepository.save(record);
    }

    public IssueRecord returnBook(Long issueId) {
        IssueRecord record = issueRecordRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue record not found"));

        if ("RETURNED".equals(record.getStatus())) {
            throw new RuntimeException("Book already returned");
        }

        LocalDate today = LocalDate.now();
        record.setReturnDate(today);
        record.setStatus("RETURNED");

        long lateDays = ChronoUnit.DAYS.between(record.getDueDate(), today);
        record.setFineAmount(calculateFine(lateDays));

        Book book = record.getBook();
        book.setAvailable(book.getAvailable() + 1);
        bookRepository.save(book);

        return issueRecordRepository.save(record);
    }
}