package dev.magadiflo.book.network.app.feedback;

import dev.magadiflo.book.network.app.book.Book;
import dev.magadiflo.book.network.app.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "feedbacks")
public class Feedback extends BaseEntity {
    private Double note;
    private String comment;

    @JoinColumn(name = "book_id")
    @ManyToOne
    private Book book;
}
