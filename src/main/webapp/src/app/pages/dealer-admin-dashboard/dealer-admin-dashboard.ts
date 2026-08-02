import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { AppAvatar, AppButton, DataTable, PageHeader, StatCard } from '../../components';

@Component({
  selector: 'app-dealer-admin-dashboard',
  imports: [CommonModule, MatIconModule, MatTabsModule,
    PageHeader, StatCard, AppButton, DataTable, AppAvatar],
  templateUrl: './dealer-admin-dashboard.html',
})
export class DealerAdminDashboard implements OnInit {
  dealerId: number | null = null;
  carCount = 0;
  employeeCount = 0;
  packageName = '';
  packageLimits = '';
  location = '';
  cars: any[] = [];
  employees: any[] = [];

  constructor(
    private authService: AuthService,
    private router: Router,
    private http: HttpClient,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.dealerId = this.authService.getDealerId();
    if (this.dealerId) this.loadStats();
  }

  loadStats() {
    this.http.get<any>(`/api/v1/dealers/${this.dealerId}/dashboard-summary`).subscribe(summary => {
      this.location = summary.location || '';
      this.carCount = summary.carCount ?? 0;
      this.employeeCount = summary.employeeCount ?? 0;
      this.packageName = summary.packageName || '';
      this.packageLimits = summary.packageName
        ? `${summary.maxEmployeeSeats} seats · ${summary.maxCarListings} listings`
        : '';
      this.cd.detectChanges();
    });

    this.http.get<any>(`/api/v1/dealers/${this.dealerId}`).subscribe(d => {
      this.cars = d.cars || [];
      this.employees = d.employees || [];
      this.cd.detectChanges();
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
