import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DealerService {

  private baseUrl = '/api/v1/dealers';

  constructor(private http: HttpClient) {}

  getAll(): Observable<any[]> {
    return this.http.get<any[]>(this.baseUrl);
  }

  getById(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${id}`);
  }

  addDealer(dealer: any): Observable<any> {
    return this.http.post<any>(this.baseUrl, dealer);
  }

  deleteDealer(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }

  getInventoryStats(): Observable<any> {
    return this.http.get(`${this.baseUrl}/stats/inventory`);
  }
}
