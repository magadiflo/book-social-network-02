package dev.magadiflo.book.network.app.handler;

import dev.magadiflo.book.network.app.exception.OperationNotPermittedException;
import jakarta.mail.MessagingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashSet;
import java.util.Set;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LockedException.class)
    public ResponseEntity<ExceptionResponse> handleException(LockedException exception) {
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .businessErrorCode(BusinessErrorCodes.ACCOUNT_LOCKED.getCode())
                .businessErrorDescription(BusinessErrorCodes.ACCOUNT_LOCKED.getDescription())
                .error(exception.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exceptionResponse);
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<ExceptionResponse> handleException(DisabledException exception) {
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .businessErrorCode(BusinessErrorCodes.ACCOUNT_DISABLED.getCode())
                .businessErrorDescription(BusinessErrorCodes.ACCOUNT_DISABLED.getDescription())
                .error(exception.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exceptionResponse);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ExceptionResponse> handleException(BadCredentialsException exception) {
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .businessErrorCode(BusinessErrorCodes.BAD_CREDENTIALS.getCode())
                .businessErrorDescription(BusinessErrorCodes.BAD_CREDENTIALS.getDescription())
                .error(BusinessErrorCodes.BAD_CREDENTIALS.getDescription())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exceptionResponse);
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ExceptionResponse> handleException(MessagingException exception) {
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .error(exception.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleException(MethodArgumentNotValidException exception) {
        Set<String> errors = new HashSet<>();
        exception.getBindingResult().getAllErrors().forEach(error -> {
            String errorMessage = error.getDefaultMessage();
            errors.add(errorMessage);
        });

        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .validationErrors(errors)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    @ExceptionHandler(OperationNotPermittedException.class)
    public ResponseEntity<ExceptionResponse> handleException(OperationNotPermittedException exception) {
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .error(exception.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception exception) {
        // Registrar la excepción. En nuestro caso, imprimiremos en consola la pila de errores para
        exception.printStackTrace();
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .businessErrorDescription("Error interno, contacte al administrador")
                .error(exception.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionResponse);
    }
}