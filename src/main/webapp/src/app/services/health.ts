import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, interval, BehaviorSubject } from 'rxjs';
import { catchError, switchMap, tap } from 'rxjs/operators';

export interface HealthResponse {
  status: string;
}

@Injectable({
  providedIn: 'root',
})
export class Health {
  private healthUrl = '/actuator/health';
  private healthStatusSubject = new BehaviorSubject<string>('UNKNOWN');
  public healthStatus$ = this.healthStatusSubject.asObservable();

  constructor(private http: HttpClient) {
    // Check health every 30 seconds
    this.startHealthCheck();
  }

  private startHealthCheck(): void {
    // Initial check
    this.checkHealth().subscribe({
      error: () => {} // Ignore errors during initial check
    });

    // Periodic check every 30 seconds
    interval(30000)
      .pipe(
        switchMap(() => this.checkHealth())
      )
      .subscribe({
        error: () => {} // Ignore errors during periodic checks
      });
  }

  checkHealth(): Observable<HealthResponse> {
    return this.http.get<HealthResponse>(this.healthUrl)
      .pipe(
        tap(response => {
          this.healthStatusSubject.next(response.status || 'UNKNOWN');
        }),
        catchError((error) => {
          this.healthStatusSubject.next('DOWN');
          throw error;
        })
      );
  }

  getHealthStatus(): string {
    return this.healthStatusSubject.value;
  }
}
