import { Component, OnInit } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { HealthIndicator } from '../../components/health-indicator/health-indicator';

@Component({
  selector: 'app-dealer-admin-dashboard',
  imports: [CommonModule, RouterLink,
    MatToolbarModule, MatSidenavModule, MatListModule,
    MatIconModule, MatButtonModule, MatCardModule, HealthIndicator],
  templateUrl: './dealer-admin-dashboard.html',
  styleUrl: './dealer-admin-dashboard.css',
})
export class DealerAdminDashboard implements OnInit {
  dealerId: number | null = null;
  constructor(private authService: AuthService, private router: Router) {}
  ngOnInit() { this.dealerId = this.authService.getDealerId(); }
  logout() { this.authService.logout(); this.router.navigate(['/login']); }
}
