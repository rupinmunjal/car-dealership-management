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
  dealerId: number | null;
  permissions: string[];
}

export interface JwtPayload {
  sub: string;
  role: 'SITE_ADMIN' | 'DEALER_ADMIN' | 'DEALER_EMPLOYEE';
  dealerId: number | null;
  permissions: string[];
  dealerStatus: 'ACTIVE' | 'SUSPENDED';
  exp: number;
  iat: number;
}

export type UserRole = 'SITE_ADMIN' | 'DEALER_ADMIN' | 'DEALER_EMPLOYEE';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private baseUrl = '/api/v1/auth';
  private tokenKey = 'auth_token';

  private currentUserSubject = new BehaviorSubject<JwtPayload | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  constructor(private http: HttpClient) {
    this.loadTokenFromStorage();
  }

  // ── Authentication API calls ──────────────────────────────────

  register(request: AuthenticationRequest): Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(`${this.baseUrl}/register`, request)
      .pipe(tap(response => this.handleAuthResponse(response)));
  }

  login(request: AuthenticationRequest): Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(`${this.baseUrl}/login`, request)
      .pipe(tap(response => this.handleAuthResponse(response)));
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    this.currentUserSubject.next(null);
  }

  // ── Token management ──────────────────────────────────────────

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  isLoggedIn(): boolean {
    const token = this.getToken();
    if (!token) return false;
    try {
      const payload = this.decodeToken(token);
      if (payload && payload.exp) {
        if (new Date(payload.exp * 1000) <= new Date()) {
          this.logout();
          return false;
        }
      }
      return true;
    } catch {
      this.logout();
      return false;
    }
  }

  // ── User info derived from JWT ────────────────────────────────

  getCurrentUser(): JwtPayload | null {
    return this.currentUserSubject.value;
  }

  getRole(): UserRole | null {
    return this.currentUserSubject.value?.role ?? null;
  }

  getDealerId(): number | null {
    return this.currentUserSubject.value?.dealerId ?? null;
  }

  getPermissions(): string[] {
    return this.currentUserSubject.value?.permissions ?? [];
  }

  getEmail(): string | null {
    return this.currentUserSubject.value?.sub ?? null;
  }

  // ── Role checks ───────────────────────────────────────────────

  isSiteAdmin(): boolean {
    return this.getRole() === 'SITE_ADMIN';
  }

  isDealerAdmin(): boolean {
    return this.getRole() === 'DEALER_ADMIN';
  }

  isDealerEmployee(): boolean {
    return this.getRole() === 'DEALER_EMPLOYEE';
  }

  hasPermission(permission: string): boolean {
    return this.isSiteAdmin() || this.isDealerAdmin() || this.getPermissions().includes(permission);
  }

  isSuspended(): boolean {
    return this.currentUserSubject.value?.dealerStatus === 'SUSPENDED';
  }

  /** Legacy compatibility */
  isAdmin(): boolean {
    return this.isSiteAdmin() || this.isDealerAdmin();
  }

  // ── Private helpers ───────────────────────────────────────────

  private handleAuthResponse(response: AuthenticationResponse): void {
    if (response.token) {
      localStorage.setItem(this.tokenKey, response.token);
      const payload = this.decodeToken(response.token);
      this.currentUserSubject.next(payload);
    }
  }

  private loadTokenFromStorage(): void {
    const token = this.getToken();
    if (token) {
      const payload = this.decodeToken(token);
      if (payload) {
        const now = new Date().getTime() / 1000;
        if (payload.exp && payload.exp > now) {
          this.currentUserSubject.next(payload);
        } else {
          this.logout();
        }
      }
    }
  }

  // ── Dashboard routing helper ─────────────────────────────────

  getDashboardPath(): string {
    switch (this.getRole()) {
      case 'SITE_ADMIN': return '/dashboard/site-admin';
      case 'DEALER_ADMIN': return '/dashboard/dealer-admin';
      case 'DEALER_EMPLOYEE': return '/dashboard/dealer-employee';
      default: return '/login';
    }
  }

  // ── Private helpers ───────────────────────────────────────────

  private decodeToken(token: string): JwtPayload | null {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64).split('').map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join('')
      );
      const raw = JSON.parse(jsonPayload);
      return {
        sub: raw.sub ?? '',
        role: raw.role ?? 'DEALER_EMPLOYEE',
        dealerId: raw.dealerId ?? null,
        permissions: raw.permissions ?? [],
        dealerStatus: raw.dealerStatus ?? 'ACTIVE',
        exp: raw.exp ?? 0,
        iat: raw.iat ?? 0
      };
    } catch {
      return null;
    }
  }
}
