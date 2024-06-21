package dev.magadiflo.book.network.app.handler;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BusinessErrorCodes {

    NO_CODE(0, HttpStatus.NOT_IMPLEMENTED, "Sin código"),
    INCORRECT_CURRENT_PASSWORD(300, HttpStatus.BAD_REQUEST, "La contraseña actual es incorrecta"),
    NEW_PASSWORD_DOES_NOT_MATCH(301, HttpStatus.BAD_REQUEST, "La nueva contraseña no coincide"),
    ACCOUNT_LOCKED(302, HttpStatus.FORBIDDEN, "La cuenta del usuario está bloqueada"),
    ACCOUNT_DISABLED(303, HttpStatus.FORBIDDEN, "La cuenta del usuario está deshabilitada"),
    BAD_CREDENTIALS(304, HttpStatus.FORBIDDEN, "Usuario y/o contraseña es incorrecta");

    private final int code;
    private final HttpStatus httpStatus;
    private final String description;

    BusinessErrorCodes(int code, HttpStatus httpStatus, String description) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.description = description;
    }
}