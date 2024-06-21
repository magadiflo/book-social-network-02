package dev.magadiflo.book.network.app.book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    @Query("""
            SELECT b
            FROM Book AS b
            WHERE b.archived = false AND
                    b.shareable = true AND
                    b.owner.id != :userId
            """)
    Page<Book> findAllDisplayableBooks(Pageable pageable, Long userId);
}
