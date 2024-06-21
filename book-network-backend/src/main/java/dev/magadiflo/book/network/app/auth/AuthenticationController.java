package dev.magadiflo.book.network.app.auth;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "API de autenticación de usuario")
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping(path = "/register")
    @ResponseStatus(HttpStatus.ACCEPTED) //<-- Solo es para forzar que OpenAPI/Swagger detecte el status de retorno
    public ResponseEntity<Void> register(@Valid @RequestBody RegistrationRequest request) throws MessagingException {
        this.authenticationService.register(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping(path = "/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(this.authenticationService.authenticate(request));
    }

    @GetMapping(path = "/activate-account")
    public void confirm(@RequestParam String token) throws MessagingException {
        this.authenticationService.activateAccount(token);
    }

}
