package dev.magadiflo.book.network.app.book;

import jakarta.validation.constraints.NotBlank;

public record BookRequest(Long id,

                          @NotBlank(message = "100")
                          String title,

                          @NotBlank(message = "101")
                          String authorName,

                          @NotBlank(message = "102")
                          String isbn,

                          @NotBlank(message = "103")
                          String synopsis,

                          boolean shareable) {
}
