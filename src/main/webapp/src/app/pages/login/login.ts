import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { AuthService, AuthenticationRequest } from '../../services/auth';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { AppAlert, AppButton, AuthShell } from '../../components';

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule, MatIconModule,
    AppAlert, AppButton, AuthShell],
  templateUrl: './login.html',
})
export class Login implements OnInit {

  credentials: AuthenticationRequest = { email: '', password: '' };
  error: string = '';
  loading: boolean = false;
  hidePassword: boolean = true;
  returnUrl: string = '/';

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
  }

  onSubmit() {
    this.error = '';
    this.loading = true;
    this.authService.login(this.credentials).subscribe({
      next: () => {
        this.loading = false;
        const dashboardPath = this.returnUrl !== '/' ? this.returnUrl : this.authService.getDashboardPath();
        this.router.navigate([dashboardPath]);
      },
      error: () => {
        this.loading = false;
        this.error = 'Invalid email or password';
      }
    });
  }
}
