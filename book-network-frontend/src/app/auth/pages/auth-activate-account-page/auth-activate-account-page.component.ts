import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';

import { CodeInputModule } from 'angular-code-input';

import { AuthenticationService } from '../../../services/services';

@Component({
  selector: 'auth-activate-account-page',
  standalone: true,
  imports: [CodeInputModule],
  templateUrl: './auth-activate-account-page.component.html',
  styleUrl: './auth-activate-account-page.component.scss'
})
export default class AuthActivateAccountPageComponent {

  private _router = inject(Router);
  private _authenticationService = inject(AuthenticationService);

  public message = '';
  public isOk = true;
  public submitted = false;

  public onCodeCompleted(activationCode: string) {
    console.log({ activationCode });
    this._authenticationService.confirm({ token: activationCode })
      .subscribe({
        next: () => {
          this.message = 'Tu cuenta ha sido activada exitosamente.\nAhora puedes proceder a iniciar sesión.';
          this.submitted = true;
          this.isOk = true;
        },
        error: err => {
          console.log(err);
          this.message = err.error.error;
          this.submitted = true;
          this.isOk = false;
        }
      });

  }

  public redirectToLogin() {
    this._router.navigate(['/auth', 'login']);
  }

}
