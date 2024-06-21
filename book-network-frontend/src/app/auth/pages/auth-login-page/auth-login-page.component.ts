import { Component, inject } from '@angular/core';
import { FormGroup, NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthenticationService } from '../../../services/services';
import { TokenService } from '../../services/token.service';

@Component({
  selector: 'auth-login-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './auth-login-page.component.html',
  styleUrl: './auth-login-page.component.scss'
})
export class AuthLoginPageComponent {

  private _formBuilder = inject(NonNullableFormBuilder);
  private _router = inject(Router);
  private _authenticationService = inject(AuthenticationService);
  private _tokenService = inject(TokenService);

  public errorMessages: string[] = [];
  public form: FormGroup = this._formBuilder.group({
    email: this._formBuilder.control<string>('martin@gmail.com'),
    password: this._formBuilder.control<string>('12345678'),
  });

  public login(): void {
    this.errorMessages = [];
    this._authenticationService.authenticate({ body: this.form.value })
      .subscribe({
        next: response => {
          console.log(response);
          this._tokenService.token = response.token as string;
          this._router.navigate(['/books']);
        },
        error: err => {
          console.log(err);
          if (err.error.validationErrors) {
            this.errorMessages = err.error.validationErrors;
          } else {
            console.log(err.error);
            this.errorMessages.push(err.error.businessErrorDescription);
          }
        }
      });
  }

  public register(): void {
    this._router.navigate(['/auth', 'register']);
  }

}
