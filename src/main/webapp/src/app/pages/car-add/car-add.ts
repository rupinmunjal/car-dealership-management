import { ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import { CarService } from '../../services/car';
import { DealerService } from '../../services/dealer';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { AppAlert, AppButton, PageHeader } from '../../components';
import { AuthService } from '../../services/auth';
import { getApiErrorMessage } from '../../utils/api-error';

@Component({
  selector: 'app-car-add',
  imports: [CommonModule, FormsModule,
    MatCardModule, MatFormFieldModule, MatInputModule, MatSelectModule, MatIconModule,
    AppAlert, AppButton, PageHeader],
  templateUrl: './car-add.html',
})
export class CarAdd implements OnInit {

  car = { make: '', model: '', modelYear: new Date().getFullYear() };
  dealers: any[] = [];
  selectedDealerId: number | null = null;
  isSiteAdmin = false;
  message: string = '';
  error: string = '';
  submitting = false;

  @ViewChild('carForm') carForm?: NgForm;

  constructor(
    private carService: CarService,
    private dealerService: DealerService,
    private auth: AuthService,
    private router: Router,
    private cd: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.isSiteAdmin = this.auth.isSiteAdmin();
    if (this.isSiteAdmin) {
      this.loadDealers();
    } else {
      this.selectedDealerId = this.auth.getDealerId();
    }
  }

  loadDealers(): void {
    this.dealerService.getAll().subscribe({
      next: data => {
        this.dealers = data;
        this.cd.detectChanges();
      },
      error: e => {
        this.error = getApiErrorMessage(e, 'Failed to load dealers');
        this.cd.detectChanges();
      }
    });
  }

  onSubmit(): void {
    if (this.selectedDealerId === null) {
      this.error = this.isSiteAdmin
        ? 'Please select a dealer'
        : 'Your account is not assigned to a dealer';
      return;
    }
    this.submitting = true;
    this.carService.addCar(this.car, this.selectedDealerId).subscribe({
      next: () => {
        this.message = 'Car added successfully!';
        this.error = '';
        this.clearForm();
        this.submitting = false;
        this.cd.detectChanges();
        setTimeout(() => this.router.navigate(['/cars']), 1000);
      },
      error: e => {
        this.error = getApiErrorMessage(e, 'Failed to add car. Please try again.');
        this.message = '';
        this.submitting = false;
        this.cd.detectChanges();
      }
    });
  }

  clearForm(): void {
    this.car = { make: '', model: '', modelYear: new Date().getFullYear() };
    this.selectedDealerId = this.isSiteAdmin ? null : this.auth.getDealerId();
    this.carForm?.resetForm({ ...this.car, dealerId: this.selectedDealerId });
  }

  resetForm(): void {
    this.clearForm();
    this.message = '';
    this.error = '';
  }
}
