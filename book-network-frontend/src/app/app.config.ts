import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { APP_ROUTES } from './app.routes';
import { httpTokenInterceptor } from './interceptors/http-token.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(APP_ROUTES),
    provideHttpClient(withInterceptors([httpTokenInterceptor])),
  ]
};
