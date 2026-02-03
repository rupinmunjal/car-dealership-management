import { Component } from '@angular/core';
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
  selector: 'app-site-admin-dashboard',
  imports: [CommonModule, RouterLink,
    MatToolbarModule, MatSidenavModule, MatListModule,
    MatIconModule, MatButtonModule, MatCardModule, HealthIndicator],
  templateUrl: './site-admin-dashboard.html',
  styleUrl: './site-admin-dashboard.css',
})
export class SiteAdminDashboard {
  constructor(private authService: AuthService, private router: Router) {}
  logout() { this.authService.logout(); this.router.navigate(['/login']); }
}
