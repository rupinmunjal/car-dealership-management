import { Component } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { AuthService, AuthenticationRequest } from '../../services/auth';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { AppAlert, AppButton, AuthShell } from '../../components';

@Component({
  selector: 'app-register',
  imports: [RouterLink, CommonModule, FormsModule, MatIconModule,
    AppAlert, AppButton, AuthShell],
  templateUrl: './register.html',
})
export class Register {

  user = { email: '', password: '', confirmPassword: '' };
  error: string = '';
  loading: boolean = false;
  hidePassword: boolean = true;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit() {
    this.error = '';
    if (this.user.password !== this.user.confirmPassword) {
      this.error = 'Passwords do not match';
      return;
    }
    if (this.user.password.length < 6) {
      this.error = 'Password must be at least 6 characters';
      return;
    }
    this.loading = true;
    const credentials: AuthenticationRequest = {
      email: this.user.email,
      password: this.user.password
    };
    this.authService.register(credentials).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/']);
      },
      error: () => {
        this.loading = false;
        this.error = 'Registration failed. Email may already be in use.';
      }
    });
  }
}
