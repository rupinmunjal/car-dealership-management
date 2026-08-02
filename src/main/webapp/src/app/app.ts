import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './services/auth';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { BreakpointObserver } from '@angular/cdk/layout';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet, RouterLink, RouterLinkActive, CommonModule,
    MatToolbarModule, MatSidenavModule, MatListModule, MatIconModule, MatButtonModule
  ],
  templateUrl: './app.html',
  styleUrls: ['./app.css']
})
export class AppComponent {
  isMobile = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    breakpointObserver: BreakpointObserver
  ) {
    breakpointObserver.observe('(max-width: 767px)').subscribe(result => {
      this.isMobile = result.matches;
    });
  }

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  currentUserEmail(): string | null {
    return this.authService.getEmail();
  }

  isSiteAdmin(): boolean {
    return this.authService.isSiteAdmin();
  }

  isDealerAdmin(): boolean {
    return this.authService.isDealerAdmin();
  }

  isDealerEmployee(): boolean {
    return this.authService.isDealerEmployee();
  }

  canAddCar(): boolean {
    return this.authService.hasPermission('CAN_ADD_CAR');
  }

  getDashboardPath(): string {
    return this.authService.getDashboardPath();
  }

  getRoleTitle(): string {
    const role = this.authService.getRole();
    switch (role) {
      case 'SITE_ADMIN': return 'Site Admin';
      case 'DEALER_ADMIN': return 'Dealer Admin';
      case 'DEALER_EMPLOYEE': return 'Employee';
      default: return '';
    }
  }

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
