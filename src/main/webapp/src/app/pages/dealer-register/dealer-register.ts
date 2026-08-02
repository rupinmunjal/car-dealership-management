import { ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { AppAlert, AppButton, PageHeader } from '../../components';
import { getApiErrorMessage } from '../../utils/api-error';

@Component({
  selector: 'app-dealer-register',
  imports: [CommonModule, FormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatIconModule,
    AppAlert, AppButton, PageHeader],
  templateUrl: './dealer-register.html',
})
export class DealerRegister implements OnInit {
  form = {
    name: '',
    location: '',
    adminEmail: '',
    adminPassword: '',
    packageId: null as number | null,
    displayName: '',
    description: ''
  };
  packages: any[] = [];
  message: string = '';
  error: string = '';
  submitting = false;
  @ViewChild('dealerForm') dealerForm?: NgForm;

  constructor(private http: HttpClient, private cd: ChangeDetectorRef) {}

  ngOnInit() {
    this.http.get<any>('/api/v1/packages', { params: { size: 100, sort: 'name,asc' } }).subscribe({
      next: (response) => {
        const pkgs = response.content ?? response;
        this.packages = pkgs;
        if (pkgs && pkgs.length > 0) {
          this.form.packageId = pkgs[0].id;
        }
        this.cd.detectChanges();
      },
      error: (e) => {
        this.error = getApiErrorMessage(e, 'Failed to load subscription packages');
      }
    });
  }

  onSubmit() {
    this.submitting = true;
    this.http.post('/api/v1/dealers/register', this.form).subscribe({
      next: () => {
        this.message = 'Dealer registered successfully!';
        this.error = '';
        this.form = this.emptyForm();
        this.dealerForm?.resetForm(this.form);
        this.submitting = false;
        this.cd.detectChanges();
      },
      error: (e) => {
        this.error = getApiErrorMessage(e, 'Registration failed');
        this.message = '';
        this.submitting = false;
        this.cd.detectChanges();
      }
    });
  }

  private emptyForm() {
    return {
      name: '',
      location: '',
      adminEmail: '',
      adminPassword: '',
      packageId: this.packages.length > 0 ? this.packages[0].id : null,
      displayName: '',
      description: ''
    };
  }
}
