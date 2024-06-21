package dev.magadiflo.book.network.app.book;

import dev.magadiflo.book.network.app.common.BaseEntity;
import dev.magadiflo.book.network.app.feedback.Feedback;
import dev.magadiflo.book.network.app.history.BookTransactionHistory;
import dev.magadiflo.book.network.app.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "books")
public class Book extends BaseEntity {
    private String title;
    private String authorName;
    private String isbn;
    private String synopsis;
    private String bookCover;
    private boolean archived;
    private boolean shareable;

    @JoinColumn(name = "owner_id")
    @ManyToOne
    private User owner;

    @OneToMany(mappedBy = "book")
    private List<Feedback> feedbacks;

    @OneToMany(mappedBy = "book")
    private List<BookTransactionHistory> histories;

    @Transient
    public double getRate() {
        if (this.feedbacks == null || this.feedbacks.isEmpty()) {
            return 0D;
        }
        double rate = this.feedbacks.stream()
                .mapToDouble(Feedback::getNote)
                .average()
                .orElse(0D);
        return Math.round(rate * 10D) / 10D;
    }
}
