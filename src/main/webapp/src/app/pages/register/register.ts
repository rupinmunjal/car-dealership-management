import { Component } from '@angular/core';
import { RouterLink, Router } from '@angular/router';
import { AuthService, AuthenticationRequest } from '../../services/auth';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-register',
  imports: [RouterLink, CommonModule, FormsModule],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {

  user = {
    email: '',
    password: '',
    confirmPassword: ''
  };

  error: string = '';
  loading: boolean = false;

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
      next: (response) => {
        this.loading = false;
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.loading = false;
        this.error = 'Registration failed. Username may already exist.';
        console.error(err);
      }
    });
  }
}
