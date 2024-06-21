import { Injectable } from '@angular/core';
import { JwtHelperService } from '@auth0/angular-jwt';

@Injectable({
  providedIn: 'root'
})
export class TokenService {

  public set token(token: string) {
    localStorage.setItem('token', token);
  }

  public get token(): string {
    return localStorage.getItem('token') as string;
  }

  public get fullName(): string {
    if (this.isTokenNotValid()) {
      return '';
    }
    const token = this.token;
    const jwtHelper = new JwtHelperService();
    const decodeToken = jwtHelper.decodeToken(token);
    return decodeToken.fullName;
  }

  public logout() {
    localStorage.clear();
    window.location.reload();
  }

  public isTokenNotValid(): boolean {
    return !this.isTokenValid();
  }

  public isTokenValid(): boolean {
    const token = this.token;
    if (!token) {
      return false
    }

    const jwtHelper = new JwtHelperService();
    const isTokenExpired = jwtHelper.isTokenExpired(token);

    if (isTokenExpired) {
      this.logout();
      return false;
    }

    return true;
  }

}
