package com.example.EBook_Management_BE.controllers;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.EBook_Management_BE.components.LocalizationUtils;
import com.example.EBook_Management_BE.dtos.BookDTO;
import com.example.EBook_Management_BE.entity.Author;
import com.example.EBook_Management_BE.entity.Book;
import com.example.EBook_Management_BE.entity.Category;
import com.example.EBook_Management_BE.entity.Painter;
import com.example.EBook_Management_BE.entity.User;
import com.example.EBook_Management_BE.entity.UserBook;
import com.example.EBook_Management_BE.constants.StatusUserBook;
import com.example.EBook_Management_BE.constants.Uri;
import com.example.EBook_Management_BE.mappers.BookMapper;
import com.example.EBook_Management_BE.responses.BookResponse;
import com.example.EBook_Management_BE.services.author.IAuthorService;
import com.example.EBook_Management_BE.services.book.IBookRedisService;
import com.example.EBook_Management_BE.services.book.IBookService;
import com.example.EBook_Management_BE.services.category.ICategoryService;
import com.example.EBook_Management_BE.services.painter.IPainterService;
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
	private final IPainterService painterService;
	private final IAuthorService authorService;

	private final LocalizationUtils localizationUtils;

	@PostMapping()
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
	@ResponseStatus(code = HttpStatus.CREATED)
	public ResponseEntity<ResponseObject> createBook(@RequestHeader(name = "user_id") Long userId, @Valid @RequestBody BookDTO bookDTO) throws Exception {
		Book book = bookMapper.mapToBookEntity(bookDTO);
		
		Set<Category> categories = new HashSet<>();
		for (Long categoryId : bookDTO.getCategoryIds()) {
			Category category = categoryService.getCategoryById(categoryId);
			category.setBooks(Set.of(book));
			categories.add(category);
		}
		
		Set<Painter> painters = new HashSet<>();
		for (Long painterId : bookDTO.getPainterIds()) {
			Painter painter = painterService.getPainterById(painterId);
			painter.setBooks(Set.of(book));
			painters.add(painter);
		}
		
		Set<Author> authors = new HashSet<>();
		for (Long authorId : bookDTO.getAuthorIds()) {
			Author author = authorService.getAuthorById(authorId);
			author.setBooks(Set.of(book));
			authors.add(author);
		}
		book.setAuthors(authors);
		book.setCategories(categories);
		book.setPainters(painters);
		
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

	@GetMapping()
//	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
	public ResponseEntity<ResponseObject> getBookById(@RequestHeader(name = "book_id") Long bookId) throws Exception {
		Book existingBook = bookService.getBookById(bookId);
		
		BookResponse bookResponse = bookMapper.mapToBookResponse(existingBook);
		
		return ResponseEntity.ok(ResponseObject.builder()
				.status(HttpStatus.OK)
				.message(localizationUtils.getLocalizedMessage(MessageKeys.BOOK_GET_BY_ID_SUCCESSFULLY))
				.data(bookResponse)
				.build());
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
	public ResponseEntity<ResponseObject> updateBook(@RequestHeader(name = "book_id") Long bookId, @Valid @RequestBody BookDTO bookDTO) throws Exception {
		Book book = bookMapper.mapToBookEntity(bookDTO);
		
		Set<Category> categories = new HashSet<>();
		for (Long categoryId : bookDTO.getCategoryIds()) {
			Category category = categoryService.getCategoryById(categoryId);
			category.setBooks(Set.of(book));
			categories.add(category);
		}
		
		Set<Painter> painters = new HashSet<>();
		for (Long painterId : bookDTO.getPainterIds()) {
			Painter painter = painterService.getPainterById(painterId);
			painter.setBooks(Set.of(book));
			painters.add(painter);
		}
		
		Set<Author> authors = new HashSet<>();
		for (Long authorId : bookDTO.getAuthorIds()) {
			Author author = authorService.getAuthorById(authorId);
			author.setBooks(Set.of(book));
			authors.add(author);
		}
		
		book = bookService.updateBook(bookId, book);
		bookRedisService.saveBookById(bookId, book);
		
		BookResponse bookResponse = bookMapper.mapToBookResponse(book);

		return ResponseEntity.ok(ResponseObject.builder()
				.status(HttpStatus.OK)
				.message(localizationUtils.getLocalizedMessage(MessageKeys.BOOK_UPDATE_SUCCESSFULLY))
				.data(bookResponse)
				.build());
	}

	@DeleteMapping()
	@PreAuthorize("hasRole('ROLE_ADMIN') or hasRole('ROLE_USER')")
	public ResponseEntity<ResponseObject> deleteBook(@RequestHeader(name = "book_id") Long bookId) throws Exception {
		bookService.deleteBook(bookId);
		
		return ResponseEntity.ok(ResponseObject.builder()
				.status(HttpStatus.OK)
				.message(localizationUtils.getLocalizedMessage(MessageKeys.BOOK_DELETE_SUCCESSFULLY))
				.build());
	}
	
	@PutMapping("/number-read")
	public ResponseEntity<ResponseObject> updateNumberRead(@RequestHeader(name = "book_id") Long bookId) throws Exception {
		Book book = bookService.getBookById(bookId);

		book.setNumberReads(book.getNumberReads() + 1);
		int random = new Random().nextInt(2) + 1;
		if (random == 1) {
			bookService.updateBook(bookId, book);
		}

		BookResponse bookResponse = bookMapper.mapToBookResponse(book);
		bookRedisService.saveBookById(bookId, book);

		return ResponseEntity.ok(ResponseObject.builder()
				.status(HttpStatus.OK)
				.message(localizationUtils.getLocalizedMessage(MessageKeys.BOOK_UPDATE_SUCCESSFULLY))
				.data(bookResponse)
				.build());
	}

}
