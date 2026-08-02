import { ChangeDetectorRef, Component, ViewChild } from '@angular/core';
import { DealerService } from '../../services/dealer';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { AppAlert, AppButton, PageHeader } from '../../components';

@Component({
  selector: 'app-dealer-add',
  imports: [CommonModule, FormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule, MatIconModule,
    AppAlert, AppButton, PageHeader],
  templateUrl: './dealer-add.html',
})
export class DealerAdd {

  dealer = { name: '', location: '' };
  message: string = '';
  error: string = '';
  submitting = false;

  @ViewChild('dealerForm') dealerForm?: NgForm;

  constructor(
    private dealerService: DealerService,
    private router: Router,
    private cd: ChangeDetectorRef
  ) {}

  onSubmit() {
    this.error = '';
    this.submitting = true;
    this.dealerService.addDealer(this.dealer).subscribe({
      next: () => {
        this.message = 'Dealer added successfully!';
        this.error = '';
        this.clearForm();
        this.submitting = false;
        this.cd.detectChanges();
        setTimeout(() => this.router.navigate(['/dealers']), 1000);
      },
      error: () => {
        this.error = 'Failed to add dealer. Please try again.';
        this.message = '';
        this.submitting = false;
        this.cd.detectChanges();
      }
    });
  }

  clearForm() {
    this.dealer = { name: '', location: '' };
    this.dealerForm?.resetForm(this.dealer);
  }

  resetForm() {
    this.clearForm();
    this.message = '';
    this.error = '';
  }
}
