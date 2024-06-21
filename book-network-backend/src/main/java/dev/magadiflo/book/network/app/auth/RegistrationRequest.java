package dev.magadiflo.book.network.app.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(@NotBlank(message = "El nombre es obligatorio")
                                  String firstName,

                                  @NotBlank(message = "El apellido es obligatorio")
                                  String lastName,

                                  @NotBlank(message = "El correo es obligatorio")
                                  @Email(message = "El correo no tiene un formato válido")
                                  String email,

                                  @NotBlank(message = "La contraseña es obligatoria")
                                  @Size(min = 8, message = "La contraseña debería tener como mínimo 8 caracteres")
                                  String password) {
}
