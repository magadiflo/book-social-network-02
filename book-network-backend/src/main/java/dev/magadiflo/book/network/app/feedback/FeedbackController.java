package dev.magadiflo.book.network.app.feedback;

import dev.magadiflo.book.network.app.common.PageResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Feedback", description = "API Rest de la entidad Feedback")
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/feedbacks")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @GetMapping(path = "/book/{bookId}")
    public ResponseEntity<PageResponse<FeedbackResponse>> findAllFeedbackByBook(@PathVariable Long bookId,
                                                                                @RequestParam(defaultValue = "0") int page,
                                                                                @RequestParam(defaultValue = "10") int size,
                                                                                Authentication authentication) {
        return ResponseEntity.ok(this.feedbackService.findAllFeedbackByBook(bookId, page, size, authentication));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) //<-- Solo es para forzar que OpenAPI/Swagger detecte el status de retorno
    public ResponseEntity<Long> saveFeedback(@Valid @RequestBody FeedbackRequest request, Authentication authentication) {
        return new ResponseEntity<>(this.feedbackService.save(request, authentication), HttpStatus.CREATED);
    }

}
