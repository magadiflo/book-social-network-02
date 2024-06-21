import { inject } from '@angular/core';
import { CanActivateFn, CanMatchFn, Router } from '@angular/router';

import { TokenService } from '../auth/services/token.service';

export const canMatchAuthGuard: CanMatchFn = (route, segments) => {
  console.log('Ejecutnado canMatchAuthGuard()');
  return checkAuthStatus();
}

export const canActivateAuthGuard: CanActivateFn = (route, state) => {
  console.log('Ejecutnado canActivateAuthGuard()');
  return checkAuthStatus();
};

const checkAuthStatus = () => {
  const tokenService = inject(TokenService);
  const router = inject(Router);

  if (tokenService.isTokenNotValid()) {
    router.navigate(['/auth', 'login']);
    return false;
  }

  return true;
}
