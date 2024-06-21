import { Routes } from '@angular/router';

import { canMatchAuthGuard } from './guards/auth.guard';

export const APP_ROUTES: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./auth/auth.routes'),
  },
  {
    path: 'books',
    loadChildren: () => import('./books/books.routes'),
    canMatch: [canMatchAuthGuard],
  },
  { path: '**', redirectTo: '/auth', },
];
