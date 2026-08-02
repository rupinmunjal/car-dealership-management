import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { PageResponse } from './car';

@Injectable({
  providedIn: 'root'
})
export class DealerService {

  private baseUrl = '/api/v1/dealers';

  constructor(private http: HttpClient) {}

  getAll(): Observable<any[]> {
    const params = new HttpParams().set('size', '100').set('sort', 'name,asc');
    return this.http.get<any>(this.baseUrl, { params }).pipe(
      map(res => Array.isArray(res) ? res : (res?.content || []))
    );
  }

  getPage(page = 0, size = 20, sort = 'id,asc'): Observable<PageResponse<any>> {
    const params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size))
      .set('sort', sort);
    return this.http.get<PageResponse<any>>(this.baseUrl, { params });
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
