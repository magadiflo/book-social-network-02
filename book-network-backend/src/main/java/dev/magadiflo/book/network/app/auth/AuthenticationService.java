package dev.magadiflo.book.network.app.auth;

import dev.magadiflo.book.network.app.email.EmailService;
import dev.magadiflo.book.network.app.email.EmailTemplateName;
import dev.magadiflo.book.network.app.role.Role;
import dev.magadiflo.book.network.app.role.RoleRepository;
import dev.magadiflo.book.network.app.security.JwtService;
import dev.magadiflo.book.network.app.user.Token;
import dev.magadiflo.book.network.app.user.TokenRepository;
import dev.magadiflo.book.network.app.user.User;
import dev.magadiflo.book.network.app.user.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AuthenticationService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${application.mailing.frontend.activation-url}")
    private String activationUrl;

    public void register(RegistrationRequest request) throws MessagingException {
        Role defaultRoleDB = this.roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalArgumentException("El rol USER no fue encontrado"));
        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(this.passwordEncoder.encode(request.password()))
                .accountLocked(false)
                .enabled(false)
                .roles(List.of(defaultRoleDB))
                .build();
        this.userRepository.save(user);
        this.sendValidationEmail(user);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        var authentication = new UsernamePasswordAuthenticationToken(request.email(), request.password());
        Authentication authenticate = this.authenticationManager.authenticate(authentication);
        /**
         * Si el flujo llega hasta esta línea significa que la autenticación ha sido exitosa retornando un objeto
         * del tipo Authentication. En caso contrario, el método this.authenticationManager.authenticate(...)
         * lanzará una excepción
         */
        User user = (User) authenticate.getPrincipal();
        var claims = new HashMap<String, Object>();
        claims.put("fullName", user.fullName());

        String jwt = this.jwtService.generateToken(claims, user);

        return new AuthenticationResponse(jwt);
    }

    public void activateAccount(String token) throws MessagingException {
        Token tokenDB = this.tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        if (LocalDateTime.now().isAfter(tokenDB.getExpiresAt())) {
            this.sendValidationEmail(tokenDB.getUser());
            throw new RuntimeException("El token de activación ha caducado. Se ha enviado un nuevo token al mismo correo.");
        }

        // Podemos obtener el usuario desde el token recuperado o nuevamente ir a la bd
        User user = this.userRepository.findById(tokenDB.getUser().getId())
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado en la activación"));
        user.setEnabled(true);
        this.userRepository.save(user);

        tokenDB.setValidatedAt(LocalDateTime.now());
        this.tokenRepository.save(tokenDB);
    }

    private void sendValidationEmail(User user) throws MessagingException {
        String activationCode = this.generateAndSaveActivationCode(user);
        this.emailService.sendEmail(user.getEmail(), user.fullName(), EmailTemplateName.ACTIVATE_ACCOUNT,
                this.activationUrl, activationCode, "Activación de cuenta");
    }

    private String generateAndSaveActivationCode(User user) {
        String activationCode = this.generateActivationCode(6);
        Token token = Token.builder()
                .token(activationCode)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .user(user)
                .build();
        this.tokenRepository.save(token);
        return activationCode;
    }

    private String generateActivationCode(int length) {
        String characters = "0123456789";
        StringBuilder codeBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(characters.length()); // Obtenemos índices del [0..9]
            codeBuilder.append(characters.charAt(randomIndex)); // Obtenemos el carácter según el índice
        }
        return codeBuilder.toString();
    }
}
