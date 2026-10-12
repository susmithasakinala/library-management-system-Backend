package com.example.libapp.service;

import com.example.libapp.entity.Book;
import com.example.libapp.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));
    }

    public Book addBook(Book book) {
        book.setAvailable(book.getQuantity());
        return bookRepository.save(book);
    }

    public Book updateBook(Long id, Book newData) {
        Book book = getBookById(id);
        book.setTitle(newData.getTitle());
        book.setAuthor(newData.getAuthor());
        book.setIsbn(newData.getIsbn());
        book.setCategory(newData.getCategory());
        book.setQuantity(newData.getQuantity());
        book.setAvailable(newData.getAvailable());
        return bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    public List<Book> searchBooks(String keyword) {
        return bookRepository
            .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(
                keyword, keyword, keyword);
    }
}