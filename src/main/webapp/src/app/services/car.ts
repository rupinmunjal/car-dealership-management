import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface CarQuery {
  page?: number;
  size?: number;
  sort?: string;
  make?: string;
  model?: string;
  minPrice?: number | null;
  maxPrice?: number | null;
  year?: number | null;
  search?: string;
}

@Injectable({
  providedIn: 'root'
})
export class CarService {

  private baseUrl = '/api/v1/cars';

  constructor(private http: HttpClient) {}

  getPage(query: CarQuery = {}): Observable<PageResponse<any>> {
    let params = new HttpParams()
      .set('page', String(query.page ?? 0))
      .set('size', String(query.size ?? 20))
      .set('sort', query.sort ?? 'id,asc');

    for (const key of ['make', 'model', 'minPrice', 'maxPrice', 'year', 'search'] as const) {
      const value = query[key];
      if (value !== undefined && value !== null && String(value).trim() !== '') {
        params = params.set(key, String(value));
      }
    }
    return this.http.get<PageResponse<any>>(this.baseUrl, { params });
  }

  getById(id: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${id}`);
  }

  addCar(car: any, dealerId: number): Observable<any> {
    const params = new HttpParams().set('dealerId', dealerId.toString());
    return this.http.post<any>(this.baseUrl, car, { params });
  }

  updateCar(id: number, car: any): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/${id}`, car);
  }

  deleteCar(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }

  getUniqueBrandsCount(): Observable<any> {
    return this.http.get(`${this.baseUrl}/stats/brands`);
  }
}
