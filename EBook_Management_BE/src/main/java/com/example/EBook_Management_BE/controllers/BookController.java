package com.example.EBook_Management_BE.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.example.EBook_Management_BE.dtos.book.SearchBookDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.EBook_Management_BE.components.LocalizationUtils;
import com.example.EBook_Management_BE.dtos.book.BookDTO;
import com.example.EBook_Management_BE.entity.Book;
import com.example.EBook_Management_BE.entity.Category;
import com.example.EBook_Management_BE.entity.User;
import com.example.EBook_Management_BE.entity.UserBook;
import com.example.EBook_Management_BE.constants.StatusUserBook;
import com.example.EBook_Management_BE.constants.Uri;
import com.example.EBook_Management_BE.mappers.BookMapper;
import com.example.EBook_Management_BE.responses.BookResponse;
import com.example.EBook_Management_BE.services.book.IBookRedisService;
import com.example.EBook_Management_BE.services.book.IBookService;
import com.example.EBook_Management_BE.services.category.ICategoryService;
import com.example.EBook_Management_BE.services.user.IUserService;
import com.example.EBook_Management_BE.utils.MessageKeys;
import com.example.EBook_Management_BE.utils.ResponseObject;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = Uri.BOOK)
@RequiredArgsConstructor
public class BookController {
    private final IBookService bookService;
    private final IBookRedisService bookRedisService;
    private final BookMapper bookMapper;

    private final IUserService userService;
    private final ICategoryService categoryService;

    private final LocalizationUtils localizationUtils;

    @PostMapping("/{userId}")
//    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    @ResponseStatus(code = HttpStatus.CREATED)
    public ResponseEntity<ResponseObject> createBook(@PathVariable Long userId, @Valid @RequestBody BookDTO bookDTO) throws Exception {
        Book book = bookMapper.mapToBookEntity(bookDTO);
        book.setNumberReads(0L);

        Set<Category> categories = new HashSet<>();
        for (Long categoryId : bookDTO.getCategoryIds()) {
            Category category = categoryService.getCategoryById(categoryId);
            category.setBooks(Set.of(book));
            categories.add(category);
        }
        book.setCategories(categories);

        User user = userService.getUserById(userId);
        UserBook userBook = UserBook.builder()
                .status(StatusUserBook.OWNER)
                .book(book)
                .user(user)
                .build();
        book.setUserBooks(Set.of(userBook));

        bookService.createBook(book);

        BookResponse bookResponse = bookMapper.mapToBookResponse(book);

        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.CREATED)
                .data(bookResponse)
                .message(localizationUtils.getLocalizedMessage(MessageKeys.BOOK_CREATE_SUCCESSFULLY))
                .build());
    }

    @GetMapping("/{bookId}")
//	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<ResponseObject> getBookById(@PathVariable Long bookId) throws Exception {
        Book existingBook = bookService.getBookById(bookId);

        BookResponse bookResponse = bookMapper.mapToBookResponse(existingBook);

        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message(localizationUtils.getLocalizedMessage(MessageKeys.BOOK_GET_BY_ID_SUCCESSFULLY))
                .data(bookResponse)
                .build());
    }

    @PutMapping("/{bookId}")
//    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<ResponseObject> updateBook(@PathVariable Long bookId, @Valid @RequestBody BookDTO bookDTO) throws Exception {
        Book book = bookMapper.mapToBookEntity(bookDTO);

        Set<Category> categories = new HashSet<>();
        for (Long categoryId : bookDTO.getCategoryIds()) {
            Category category = categoryService.getCategoryById(categoryId);
            category.setBooks(Set.of(book));
            categories.add(category);
        }
        book.setCategories(categories);

        book = bookService.updateBook(bookId, book);
        bookRedisService.saveBookById(bookId, book);

        BookResponse bookResponse = bookMapper.mapToBookResponse(book);

        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message(localizationUtils.getLocalizedMessage(MessageKeys.BOOK_UPDATE_SUCCESSFULLY))
                .data(bookResponse)
                .build());
    }

    @DeleteMapping("/{bookId}")
//    @PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
    public ResponseEntity<ResponseObject> deleteBook(@PathVariable Long bookId) throws Exception {
        bookService.deleteBook(bookId);

        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message(localizationUtils.getLocalizedMessage(MessageKeys.BOOK_DELETE_SUCCESSFULLY))
                .build());
    }

    @PutMapping("/number-read/{bookId}")
    public ResponseEntity<ResponseObject> updateNumberRead(@PathVariable Long bookId) throws Exception {
        Book book = bookService.getBookById(bookId);

        book.setNumberReads(book.getNumberReads() + 1);
        bookService.updateBook(bookId, book);

        BookResponse bookResponse = bookMapper.mapToBookResponse(book);
        bookRedisService.saveBookById(bookId, book);

        return ResponseEntity.ok(ResponseObject.builder()
                .status(HttpStatus.OK)
                .message(localizationUtils.getLocalizedMessage(MessageKeys.BOOK_UPDATE_SUCCESSFULLY))
                .data(bookResponse)
                .build());
    }

    @GetMapping("/all/{userId}")
    public ResponseEntity<ResponseObject> getAllBookByUser(@PathVariable Long userId) throws Exception {
        User user = userService.getUserById(userId);

        List<Book> books = bookService.getAllBookByUser(user);

        List<BookResponse> bookResponses = books.stream()
                .map(bookMapper::mapToBookResponse)
                .toList();

        return ResponseEntity.ok(ResponseObject.builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.BOOK_GET_ALL_BY_USER))
                .status(HttpStatus.OK)
                .data(bookResponses)
                .build());
    }

    @GetMapping("/all/{type}/{page}/{limit}")
    public ResponseEntity<ResponseObject> getAllBookByType(@PathVariable String type, @PathVariable Long page, @PathVariable Long limit) throws Exception {
        List<Book> books = bookService.getAllBookByType(type, page - 1, limit);

        List<BookResponse> bookResponses = books.stream()
                .map(bookMapper::mapToBookResponse)
                .toList();

        return ResponseEntity.ok(ResponseObject.builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.BOOK_GET_ALL_BY_TYPE))
                .status(HttpStatus.OK)
                .data(bookResponses)
                .build());
    }

    @PostMapping("/search/{page}/{limit}")
    public ResponseEntity<ResponseObject> searchBook(@Valid @RequestBody SearchBookDTO searchBookDTO,
                                                     @PathVariable Long page,
                                                     @PathVariable Long limit) {
        Page<Book> books = bookService.searchBook(searchBookDTO, page - 1, limit);

        List<BookResponse> bookResponses = books.getContent().stream()
                .map(bookMapper::mapToBookResponse)
                .toList();

        Page<BookResponse> bookResponsesPage = new PageImpl<>(bookResponses, books.getPageable(), books.getTotalElements());

        return ResponseEntity.ok(ResponseObject.builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.BOOK_SEARCH_SUCCESSFULLY))
                .status(HttpStatus.OK)
                .data(bookResponsesPage)
                .build());
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<ResponseObject> getAllBookAccount(@PathVariable Long accountId) throws Exception {
        User user = userService.getUserById(accountId);

        List<Book> books = bookService.getBookByAccount(user);

        List<BookResponse> bookResponses = books.stream()
                .map(bookMapper::mapToBookResponse)
                .toList();

        return ResponseEntity.ok(ResponseObject.builder()
                .message(localizationUtils.getLocalizedMessage(MessageKeys.BOOK_GET_ALL_BY_USER))
                .status(HttpStatus.OK)
                .data(bookResponses)
                .build());
    }
}
