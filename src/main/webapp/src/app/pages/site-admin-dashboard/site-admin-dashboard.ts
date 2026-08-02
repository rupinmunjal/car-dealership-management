import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { ActivityLog, AppAvatar, AppButton, DataTable, PageHeader, StatCard } from '../../components';

@Component({
  selector: 'app-site-admin-dashboard',
  imports: [CommonModule, MatIconModule, MatTabsModule,
    PageHeader, StatCard, AppButton, DataTable, AppAvatar, ActivityLog],
  templateUrl: './site-admin-dashboard.html',
})
export class SiteAdminDashboard implements OnInit {
  dealers: any[] = [];
  packages: any[] = [];
  cars: any[] = [];
  totalDealers = 0;
  activeDealers = 0;
  totalCars = 0;
  totalPackages = 0;
  totalEmployees = 0;

  constructor(
    private authService: AuthService,
    private router: Router,
    private http: HttpClient,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.loadStats();
  }

  /** "N active" dealers sub-stat with the count emphasized in green. */
  get activeDealersSub(): string {
    return `<span class="text-emerald-600 font-semibold">${this.activeDealers}</span> active`;
  }

  loadStats() {
    // Fetch dealers
    this.http.get<any>('/api/v1/dealers', { params: { size: 100 } }).subscribe(res => {
      const d = Array.isArray(res) ? res : (res?.content || []);
      this.dealers = d;
      this.totalDealers = res.totalElements ?? d.length;
      this.activeDealers = d.filter((x: any) => x.status === 'ACTIVE').length;
      this.totalEmployees = 0;
      d.forEach((dealer: any) => {
        if (dealer.employees) this.totalEmployees += dealer.employees.length;
      });
      this.cd.detectChanges();
    });

    // Fetch packages
    this.http.get<any>('/api/v1/packages', { params: { size: 100 } }).subscribe(res => {
      const p = Array.isArray(res) ? res : (res?.content || []);
      this.packages = p;
      this.totalPackages = res.totalElements ?? p.length;
      this.cd.detectChanges();
    });

    // Fetch cars
    this.http.get<any>('/api/v1/cars', { params: { size: 100 } }).subscribe(res => {
      this.cars = Array.isArray(res) ? res : (res?.content || []);
      this.totalCars = res.totalElements ?? this.cars.length;
      this.cd.detectChanges();
    });
  }
}
