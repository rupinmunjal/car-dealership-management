import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';

@Component({
  selector: 'app-dealer-register',
  imports: [CommonModule, RouterLink, FormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule, MatToolbarModule],
  templateUrl: './dealer-register.html',
  styleUrl: './dealer-register.css',
})
export class DealerRegister {
  form = { name: '', location: '', adminEmail: '', adminPassword: '' };
  message: string = '';
  error: string = '';

  constructor(private http: HttpClient) {}

  onSubmit() {
    this.http.post('/api/v1/dealers/register', this.form).subscribe({
      next: () => { this.message = 'Dealer registered!'; this.error = ''; this.form = { name: '', location: '', adminEmail: '', adminPassword: '' }; },
      error: (e) => { this.error = e.error?.message || 'Registration failed'; this.message = ''; }
    });
  }
}
