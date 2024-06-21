package dev.magadiflo.book.network.app.feedback;

import dev.magadiflo.book.network.app.book.Book;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class FeedbackMapper {

    public Feedback toFeedback(FeedbackRequest request) {
        return Feedback.builder()
                .note(request.note())
                .comment(request.comment())
                .book(Book.builder().id(request.bookId()).build())
                .build();
    }


    public FeedbackResponse toFeedbackResponse(Feedback feedback, Long userId) {
        boolean itIsOwnFeedback = Objects.equals(feedback.getBook().getOwner().getId(), userId);
        return FeedbackResponse.builder()
                .note(feedback.getNote())
                .comment(feedback.getComment())
                .ownFeedback(itIsOwnFeedback)
                .build();
    }
}
