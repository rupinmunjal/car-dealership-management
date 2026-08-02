import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../services/auth';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { AppAlert, AppButton, PageHeader } from '../../components';
import { getApiErrorMessage } from '../../utils/api-error';

@Component({
  selector: 'app-dealer-settings',
  imports: [CommonModule, FormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule, MatIconModule, MatSlideToggleModule,
    AppAlert, AppButton, PageHeader],
  templateUrl: './dealer-settings.html',
})
export class DealerSettings implements OnInit {
  form = { displayName: '', description: '', visible: true };
  message = ''; error = '';
  submitting = false;
  private dealerId: number | null = null;
  constructor(
    private http: HttpClient,
    private auth: AuthService,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit() {
    this.dealerId = this.auth.getDealerId();
    if (this.dealerId) {
      this.http.get<any>(`/api/v1/dealers/${this.dealerId}`).subscribe(d => {
        this.form = { displayName: d.displayName || '', description: d.description || '', visible: d.visible ?? true };
        this.cd.detectChanges();
      });
    }
  }

  onSubmit() {
    this.submitting = true;
    this.http.put(`/api/v1/dealers/${this.dealerId}/settings`, this.form).subscribe({
      next: () => { this.message = 'Settings saved!'; this.error = ''; this.submitting = false; this.cd.detectChanges(); },
      error: (e) => { this.error = getApiErrorMessage(e, 'Save failed'); this.message = ''; this.submitting = false; this.cd.detectChanges(); }
    });
  }
}
