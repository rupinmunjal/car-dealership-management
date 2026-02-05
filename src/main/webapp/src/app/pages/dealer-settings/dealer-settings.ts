import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatToolbarModule } from '@angular/material/toolbar';

@Component({
  selector: 'app-dealer-settings',
  imports: [CommonModule, FormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatButtonModule, MatIconModule, MatSlideToggleModule, MatToolbarModule],
  templateUrl: './dealer-settings.html',
  styleUrl: './dealer-settings.css',
})
export class DealerSettings implements OnInit {
  form = { displayName: '', description: '', visible: true };
  message = ''; error = '';
  private dealerId: number | null = null;

  constructor(private http: HttpClient, private auth: AuthService) {}

  ngOnInit() {
    this.dealerId = this.auth.getDealerId();
    if (this.dealerId) {
      this.http.get<any>(`/api/v1/dealers/${this.dealerId}`).subscribe(d => {
        this.form = { displayName: d.displayName || '', description: d.description || '', visible: d.visible ?? true };
      });
    }
  }

  onSubmit() {
    this.http.put(`/api/v1/dealers/${this.dealerId}/settings`, this.form).subscribe({
      next: () => { this.message = 'Settings saved!'; this.error = ''; },
      error: (e) => { this.error = e.error?.message || 'Save failed'; this.message = ''; }
    });
  }
}
