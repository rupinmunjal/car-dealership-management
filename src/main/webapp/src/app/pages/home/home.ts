import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';

@Component({
  selector: 'app-home',
  template: '',
})
export class Home implements OnInit {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const role = this.authService.getRole();
    switch (role) {
      case 'SITE_ADMIN':
        this.router.navigate(['/dashboard/site-admin']);
        break;
      case 'DEALER_ADMIN':
        this.router.navigate(['/dashboard/dealer-admin']);
        break;
      case 'DEALER_EMPLOYEE':
        this.router.navigate(['/dashboard/dealer-employee']);
        break;
      default:
        this.authService.logout();
        this.router.navigate(['/login']);
    }
  }
}
