package dev.magadiflo.book.network.app.book;

import dev.magadiflo.book.network.app.common.PageResponse;
import dev.magadiflo.book.network.app.exception.OperationNotPermittedException;
import dev.magadiflo.book.network.app.file.FileStorageService;
import dev.magadiflo.book.network.app.history.BookTransactionHistory;
import dev.magadiflo.book.network.app.history.BookTransactionHistoryRepository;
import dev.magadiflo.book.network.app.user.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookTransactionHistoryRepository transactionHistoryRepository;
    private final BookMapper bookMapper;
    private final FileStorageService fileStorageService;

    public PageResponse<BookResponse> findAllBooks(int page, int size, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> bookPage = this.bookRepository.findAllDisplayableBooks(pageable, user.getId());
        List<BookResponse> bookResponses = bookPage.stream()
                .map(this.bookMapper::toBookResponse)
                .toList();

        return PageResponse.<BookResponse>builder()
                .content(bookResponses)
                .number(bookPage.getNumber())
                .size(bookPage.getSize())
                .totalElements(bookPage.getTotalElements())
                .totalPages(bookPage.getTotalPages())
                .first(bookPage.isFirst())
                .last(bookPage.isLast())
                .build();
    }

    public PageResponse<BookResponse> findAllBooksByOwner(int page, int size, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<Book> bookPage = this.bookRepository.findAll(BookSpecification.withOwnerId(user.getId()), pageable);
        List<BookResponse> bookResponses = bookPage.stream()
                .map(this.bookMapper::toBookResponse)
                .toList();

        return PageResponse.<BookResponse>builder()
                .content(bookResponses)
                .number(bookPage.getNumber())
                .size(bookPage.getSize())
                .totalElements(bookPage.getTotalElements())
                .totalPages(bookPage.getTotalPages())
                .first(bookPage.isFirst())
                .last(bookPage.isLast())
                .build();
    }

    public PageResponse<BorrowedBookResponse> findAllBorrowedBooks(int page, int size, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks = this.transactionHistoryRepository.findAllBorrowedBooks(pageable, user.getId());
        List<BorrowedBookResponse> bookResponses = allBorrowedBooks.stream()
                .map(this.bookMapper::toBorrowedBookResponse)
                .toList();
        return PageResponse.<BorrowedBookResponse>builder()
                .content(bookResponses)
                .number(allBorrowedBooks.getNumber())
                .size(allBorrowedBooks.getSize())
                .totalElements(allBorrowedBooks.getTotalElements())
                .totalPages(allBorrowedBooks.getTotalPages())
                .first(allBorrowedBooks.isFirst())
                .last(allBorrowedBooks.isLast())
                .build();
    }

    public PageResponse<BorrowedBookResponse> findAllReturnedBooks(int page, int size, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());
        Page<BookTransactionHistory> allBorrowedBooks = this.transactionHistoryRepository.findAllReturnedBooks(pageable, user.getId());
        List<BorrowedBookResponse> bookResponses = allBorrowedBooks.stream()
                .map(this.bookMapper::toBorrowedBookResponse)
                .toList();
        return PageResponse.<BorrowedBookResponse>builder()
                .content(bookResponses)
                .number(allBorrowedBooks.getNumber())
                .size(allBorrowedBooks.getSize())
                .totalElements(allBorrowedBooks.getTotalElements())
                .totalPages(allBorrowedBooks.getTotalPages())
                .first(allBorrowedBooks.isFirst())
                .last(allBorrowedBooks.isLast())
                .build();
    }

    public BookResponse findById(Long bookId) {
        return this.bookRepository.findById(bookId)
                .map(this.bookMapper::toBookResponse)
                .orElseThrow(() -> new EntityNotFoundException("No se encuentra el libro con el id " + bookId));
    }

    public Long save(BookRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Book book = this.bookMapper.toBook(request);
        book.setOwner(user);
        return this.bookRepository.save(book).getId();
    }

    public Long borrowBook(Long bookId, Authentication authentication) {
        Book book = this.bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el libro con id " + bookId));
        // Si el book está archivado o no es compartible, entonces lanzamos una excepción de operación no permitida
        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("El libro solicitado no se puede tomar prestado porque está archivado o no se puede compartir");
        }

        User user = (User) authentication.getPrincipal();
        if (Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("No puedes pedir prestado tu propio libro");
        }

        final boolean isAlreadyBorrowed = this.transactionHistoryRepository.isAlreadyBorrowedByUser(bookId, user.getId());
        if (isAlreadyBorrowed) {
            throw new OperationNotPermittedException("El libro solicitado ya está prestado");
        }

        BookTransactionHistory bookTransactionHistory = BookTransactionHistory.builder()
                .user(user)
                .book(book)
                .returned(false)
                .returnApproved(false)
                .build();

        return this.transactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public Long updateShareableStatus(Long bookId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Book book = this.bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el libro con id " + bookId));
        // El status del libro solo puede ser actualizado por el dueño del propio libro
        if (!Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("No puedes actualizar el estado del libro para compartir");
        }
        book.setShareable(!book.isShareable());
        this.bookRepository.save(book);
        return bookId;
    }

    public Long updateArchivedStatus(Long bookId, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Book book = this.bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el libro con id " + bookId));
        // El status del libro solo puede ser actualizado por el dueño del propio libro
        if (!Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("No puedes actualizar el estado del libro para archivar");
        }
        book.setArchived(!book.isArchived());
        this.bookRepository.save(book);
        return bookId;
    }

    public Long returnBorrowBook(Long bookId, Authentication authentication) {
        Book book = this.bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el libro con id " + bookId));
        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("El libro solicitado no se puede tomar prestado porque está archivado o no se puede compartir");
        }

        User user = (User) authentication.getPrincipal();
        if (Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("No puedes pedir prestado o retornar tu propio libro");
        }

        BookTransactionHistory bookTransactionHistory = this.transactionHistoryRepository.findByBookIdAndUserId(bookId, user.getId())
                .orElseThrow(() -> new OperationNotPermittedException("No tomaste prestado este libro"));
        bookTransactionHistory.setReturned(true);

        return this.transactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public Long approvedReturnBorrowBook(Long bookId, Authentication authentication) {
        Book book = this.bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el libro con id " + bookId));
        if (book.isArchived() || !book.isShareable()) {
            throw new OperationNotPermittedException("El libro solicitado no se puede tomar prestado porque está archivado o no se puede compartir");
        }

        User user = (User) authentication.getPrincipal();
        if (!Objects.equals(book.getOwner().getId(), user.getId())) {
            throw new OperationNotPermittedException("No puedes aprobar la devolución de un libro que no te pertenece");
        }

        BookTransactionHistory bookTransactionHistory = this.transactionHistoryRepository.findByBookIdAndOwnerId(bookId, user.getId())
                .orElseThrow(() -> new OperationNotPermittedException("Los libros aún no han sido devueltos. No puedes aprobar su devolución"));
        bookTransactionHistory.setReturnApproved(true);

        return this.transactionHistoryRepository.save(bookTransactionHistory).getId();
    }

    public Long updateBook(BookRequest request, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Book bookDB = this.bookRepository.findById(request.id())
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el libro con id " + request.id()));

        Book book = this.bookMapper.toBook(request);
        book.setBookCover(bookDB.getBookCover());
        book.setArchived(bookDB.isArchived());
        book.setOwner(user);

        return this.bookRepository.save(book).getId();
    }

    public void uploadBookCoverPicture(Long bookId, MultipartFile file, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Book book = this.bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el libro con id " + bookId));
        String bookCover = this.fileStorageService.saveFile(file, user.getId());
        book.setBookCover(bookCover);

        this.bookRepository.save(book);
    }

    public void updateUploadBookCoverPicture(Long bookId, MultipartFile file, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Book book = this.bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró el libro con id " + bookId));

        this.fileStorageService.deleteImageIfExists(book.getBookCover());

        String bookCover = this.fileStorageService.saveFile(file, user.getId());
        book.setBookCover(bookCover);

        this.bookRepository.save(book);
    }
}
