import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { SlicePipe } from '@angular/common';

import { TokenService } from '../../../auth/services/token.service';

@Component({
  selector: 'books-menu',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, SlicePipe],
  templateUrl: './menu.component.html',
  styleUrl: './menu.component.scss'
})
export class MenuComponent {

  private _tokenService = inject(TokenService);
  private _router = inject(Router);

  public logout() {
    console.log('logout()...');
    this._tokenService.logout();
    this._router.navigate(['/auth', 'login']);
  }

  public get fullName(): string {
    return this._tokenService.fullName;
  }

}
