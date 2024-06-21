package dev.magadiflo.book.network.app.book;

import dev.magadiflo.book.network.app.common.PageResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Book", description = "API de Book")
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/books")
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<PageResponse<BookResponse>> findAllBooks(@RequestParam(defaultValue = "0", required = false) int page,
                                                                   @RequestParam(defaultValue = "10", required = false) int size,
                                                                   Authentication authentication) {
        return ResponseEntity.ok(this.bookService.findAllBooks(page, size, authentication));
    }

    @GetMapping(path = "/owner")
    public ResponseEntity<PageResponse<BookResponse>> findAllBooksByOwner(@RequestParam(defaultValue = "0", required = false) int page,
                                                                          @RequestParam(defaultValue = "10", required = false) int size,
                                                                          Authentication authentication) {
        return ResponseEntity.ok(this.bookService.findAllBooksByOwner(page, size, authentication));
    }

    @GetMapping(path = "/borrowed")
    public ResponseEntity<PageResponse<BorrowedBookResponse>> findAllBorrowedBooks(@RequestParam(defaultValue = "0", required = false) int page,
                                                                                   @RequestParam(defaultValue = "10", required = false) int size,
                                                                                   Authentication authentication) {
        return ResponseEntity.ok(this.bookService.findAllBorrowedBooks(page, size, authentication));
    }

    @GetMapping(path = "/returned")
    public ResponseEntity<PageResponse<BorrowedBookResponse>> findAllReturnedBooks(@RequestParam(defaultValue = "0", required = false) int page,
                                                                                   @RequestParam(defaultValue = "10", required = false) int size,
                                                                                   Authentication authentication) {
        return ResponseEntity.ok(this.bookService.findAllReturnedBooks(page, size, authentication));
    }

    @GetMapping(path = "/{bookId}")
    public ResponseEntity<BookResponse> findBookById(@PathVariable Long bookId) {
        return ResponseEntity.ok(this.bookService.findById(bookId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) //<-- Solo es para forzar que OpenAPI/Swagger detecte el status de retorno
    public ResponseEntity<Long> saveBook(@Valid @RequestBody BookRequest request, Authentication authentication) {
        return new ResponseEntity<>(this.bookService.save(request, authentication), HttpStatus.CREATED);
    }

    @PostMapping(path = "/borrow/{bookId}")
    public ResponseEntity<Long> borrowBook(@PathVariable Long bookId, Authentication authentication) {
        return ResponseEntity.ok(this.bookService.borrowBook(bookId, authentication));
    }

    @PostMapping(path = "/cover/{bookId}", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.ACCEPTED) //<-- Solo es para forzar que OpenAPI/Swagger detecte el status de retorno
    public ResponseEntity<Void> uploadBookCoverPicture(@PathVariable Long bookId, @Parameter @RequestPart MultipartFile file, Authentication authentication) {
        this.bookService.uploadBookCoverPicture(bookId, file, authentication);
        return ResponseEntity.accepted().build();
    }

    @PutMapping
    public ResponseEntity<Long> updateBook(@Valid @RequestBody BookRequest request, Authentication authentication) {
        return ResponseEntity.ok(this.bookService.updateBook(request, authentication));
    }

    @PatchMapping(path = "/cover-update/{bookId}", consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.ACCEPTED) //<-- Solo es para forzar que OpenAPI/Swagger detecte el status de retorno
    public ResponseEntity<Void> updateUploadBookCoverPicture(@PathVariable Long bookId, @Parameter @RequestPart MultipartFile file, Authentication authentication) {
        this.bookService.updateUploadBookCoverPicture(bookId, file, authentication);
        return ResponseEntity.accepted().build();
    }

    @PatchMapping(path = "/shareable/{bookId}")
    public ResponseEntity<Long> updateShareableStatus(@PathVariable Long bookId, Authentication authentication) {
        return ResponseEntity.ok(this.bookService.updateShareableStatus(bookId, authentication));
    }

    @PatchMapping(path = "/archived/{bookId}")
    public ResponseEntity<Long> updateArchivedStatus(@PathVariable Long bookId, Authentication authentication) {
        return ResponseEntity.ok(this.bookService.updateArchivedStatus(bookId, authentication));
    }

    @PatchMapping(path = "/borrow/return/{bookId}")
    public ResponseEntity<Long> returnBorrowBook(@PathVariable Long bookId, Authentication authentication) {
        return ResponseEntity.ok(this.bookService.returnBorrowBook(bookId, authentication));
    }

    @PatchMapping(path = "/borrow/return/approved/{bookId}")
    public ResponseEntity<Long> approvedReturnBorrowBook(@PathVariable Long bookId, Authentication authentication) {
        return ResponseEntity.ok(this.bookService.approvedReturnBorrowBook(bookId, authentication));
    }
}
