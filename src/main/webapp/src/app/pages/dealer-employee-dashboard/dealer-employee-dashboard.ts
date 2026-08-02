import { Component, OnInit } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth';
import { MatIconModule } from '@angular/material/icon';
import { PageHeader } from '../../components';

@Component({
  selector: 'app-dealer-employee-dashboard',
  imports: [CommonModule, RouterLink, MatIconModule, PageHeader],
  templateUrl: './dealer-employee-dashboard.html',
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

  permissionLabel(permission: string): string {
    const labels: Record<string, string> = {
      CAN_ADD_CAR: 'Add cars',
      CAN_EDIT_CAR: 'Edit cars',
      CAN_DELETE_CAR: 'Delete cars'
    };
    return labels[permission] ?? permission;
  }

  logout() { this.authService.logout(); this.router.navigate(['/login']); }
}
