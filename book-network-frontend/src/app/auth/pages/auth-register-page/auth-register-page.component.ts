import { Component, inject } from '@angular/core';
import { FormGroup, NonNullableFormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthenticationService } from '../../../services/services';

@Component({
  selector: 'app-auth-register-page',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './auth-register-page.component.html',
  styleUrl: './auth-register-page.component.scss'
})
export default class AuthRegisterPageComponent {

  private _formBuilder = inject(NonNullableFormBuilder);
  private _router = inject(Router);
  private _authenticationService = inject(AuthenticationService);

  public errorMessages: string[] = [];
  public form: FormGroup = this._formBuilder.group({
    email: [''],
    firstName: [''],
    lastName: [''],
    password: [''],
  });

  public login(): void {
    this._router.navigate(['/auth', 'login']);
  }

  public register(): void {
    this.errorMessages = [];
    this._authenticationService.register({ body: this.form.value })
      .subscribe({
        next: () => this._router.navigate(['/auth', 'activate-account']),
        error: err => {
          console.log(err);
          this.errorMessages = err.error.validationErrors;
        },
      });
  }

}
