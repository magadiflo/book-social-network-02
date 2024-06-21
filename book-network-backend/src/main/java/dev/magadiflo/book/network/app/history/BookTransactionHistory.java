package dev.magadiflo.book.network.app.history;

import dev.magadiflo.book.network.app.book.Book;
import dev.magadiflo.book.network.app.common.BaseEntity;
import dev.magadiflo.book.network.app.user.User;
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
@Table(name = "book_transaction_history")
public class BookTransactionHistory extends BaseEntity {
    private boolean returned;
    private boolean returnApproved;

    @JoinColumn(name = "user_id")
    @ManyToOne
    private User user;

    @JoinColumn(name = "book_id")
    @ManyToOne
    private Book book;
}
