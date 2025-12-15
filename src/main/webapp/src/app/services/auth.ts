import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject } from 'rxjs';
import { tap } from 'rxjs/operators';

export interface AuthenticationRequest {
  email: string;
  password: string;
}

export interface AuthenticationResponse {
  token: string;
  role: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private baseUrl = '/api/v1/auth';
  private tokenKey = 'auth_token';
  private roleKey = 'user_role';
  private currentUserSubject: BehaviorSubject<string | null>;
  public currentUser: Observable<string | null>;

  constructor(private http: HttpClient) {
    this.currentUserSubject = new BehaviorSubject<string | null>(this.getEmailFromToken());
    this.currentUser = this.currentUserSubject.asObservable();
  }

  register(request: AuthenticationRequest): Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(`${this.baseUrl}/register`, request)
      .pipe(
        tap(response => this.handleAuthentication(response))
      );
  }

  login(request: AuthenticationRequest): Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(`${this.baseUrl}/login`, request)
      .pipe(
        tap(response => this.handleAuthentication(response))
      );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.roleKey);
    this.currentUserSubject.next(null);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) return false;

    // Check if token is expired
    try {
      const payload = this.decodeToken(token);
      if (payload && payload.exp) {
        const expirationDate = new Date(payload.exp * 1000);
        if (expirationDate <= new Date()) {
          this.logout();
          return false;
        }
      }
      return true;
    } catch (e) {
      this.logout();
      return false;
    }
  }

  getCurrentEmail(): string | null {
    return this.currentUserSubject.value;
  }

  getUserRole(): string | null {
    return localStorage.getItem(this.roleKey);
  }

  isAdmin(): boolean {
    return this.getUserRole() === 'ADMIN';
  }

  private handleAuthentication(response: AuthenticationResponse): void {
    if (response.token) {
      localStorage.setItem(this.tokenKey, response.token);
      if (response.role) {
        localStorage.setItem(this.roleKey, response.role);
      }
      const email = this.getEmailFromToken();
      this.currentUserSubject.next(email);
    }
  }

  private getEmailFromToken(): string | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      const payload = this.decodeToken(token);
      return payload?.sub || null;
    } catch (e) {
      return null;
    }
  }

  private decodeToken(token: string): any {
    try {
      const payload = token.split('.')[1];
      return JSON.parse(atob(payload));
    } catch (e) {
      return null;
    }
  }
}
