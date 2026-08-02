import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class CarService {

  private baseUrl = '/api/v1/cars';

  constructor(private http: HttpClient) {}

  getAll(): Observable<any[]> {
    return this.http.get<any>(this.baseUrl).pipe(
      map(res => Array.isArray(res) ? res : (res?.content || []))
    );
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
