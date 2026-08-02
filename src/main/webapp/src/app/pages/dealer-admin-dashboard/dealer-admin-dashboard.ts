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
    this.http.get<any>(`/api/v1/dealers/${this.dealerId}`).subscribe(d => {
      this.location = d.location || '';
      this.cars = d.cars || [];
      this.employees = d.employees || [];
      this.carCount = this.cars.length;
      this.employeeCount = this.employees.length;
      const pkg = d.dealerPackage;
      if (pkg) {
        this.packageName = pkg.name;
        this.packageLimits = `${pkg.maxEmployeeSeats} seats · ${pkg.maxCarListings} listings`;
      }
      this.cd.detectChanges();
    });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
