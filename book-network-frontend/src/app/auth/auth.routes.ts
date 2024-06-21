import { Routes } from '@angular/router';

import { AuthLayoutPageComponent } from './pages/auth-layout-page/auth-layout-page.component';
import { AuthLoginPageComponent } from './pages/auth-login-page/auth-login-page.component';

export default [
  {
    path: '',
    component: AuthLayoutPageComponent,
    children: [
      { path: 'login', component: AuthLoginPageComponent, },
      {
        path: 'register',
        loadComponent: () => import('./pages/auth-register-page/auth-register-page.component'),
      },
      {
        path: 'activate-account',
        loadComponent: () => import('./pages/auth-activate-account-page/auth-activate-account-page.component'),
      },
      { path: '**', redirectTo: 'login', },
    ],
  }
] as Routes;
