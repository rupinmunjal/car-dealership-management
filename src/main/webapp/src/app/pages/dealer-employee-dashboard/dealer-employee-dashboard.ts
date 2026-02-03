import { Component, OnInit } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { HealthIndicator } from '../../components/health-indicator/health-indicator';

@Component({
  selector: 'app-dealer-employee-dashboard',
  imports: [CommonModule, RouterLink,
    MatToolbarModule, MatCardModule, MatChipsModule,
    MatIconModule, MatButtonModule, HealthIndicator],
  templateUrl: './dealer-employee-dashboard.html',
  styleUrl: './dealer-employee-dashboard.css',
})
export class DealerEmployeeDashboard implements OnInit {
  email: string = '';
  permissions: string[] = [];
  canAddCar: boolean = false;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit() {
    this.email = this.authService.getEmail() ?? '';
    this.permissions = this.authService.getPermissions();
    this.canAddCar = this.authService.hasPermission('CAN_ADD_CAR');
  }

  logout() { this.authService.logout(); this.router.navigate(['/login']); }
}
