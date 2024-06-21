package dev.magadiflo.book.network.app.feedback;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FeebackRepository extends JpaRepository<Feedback, Long> {
    @Query("""
            SELECT f
            FROM Feedback AS f
            WHERE f.book.id = :bookId
            """)
    Page<Feedback> findAllByBookId(Long bookId, Pageable pageable);
}
