import { Routes } from '@angular/router';

import { BookLayoutPageComponent } from './pages/book-layout-page/book-layout-page.component';
import { BookListComponent } from './pages/book-list/book-list.component';
import { canActivateAuthGuard } from '../guards/auth.guard';

export default [
  {
    path: '',
    component: BookLayoutPageComponent,
    canActivate: [canActivateAuthGuard],
    children: [
      {
        path: '',
        component: BookListComponent,
      },
      {
        path: 'my-books',
        loadComponent: () => import('./pages/my-books/my-books.component')
      },
      {
        path: 'my-borrowed-books',
        loadComponent: () => import('./pages/borrowed-book-list/borrowed-book-list.component')
      },
      {
        path: 'my-returned-books',
        loadComponent: () => import('./pages/return-books/return-books.component')
      },
      {
        path: 'manage',
        loadComponent: () => import('./pages/manage-book/manage-book.component')
      },
      {
        path: 'manage/:bookId',
        loadComponent: () => import('./pages/manage-book/manage-book.component')
      },
      { path: '**', redirectTo: '', },
    ],
  }
] as Routes;
