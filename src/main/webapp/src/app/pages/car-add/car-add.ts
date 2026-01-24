import { Component, OnInit } from '@angular/core';
import { CarService } from '../../services/car';
import { DealerService } from '../../services/dealer';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-car-add',
  imports: [CommonModule, FormsModule, RouterLink,
    MatCardModule, MatFormFieldModule, MatInputModule,
    MatSelectModule, MatButtonModule, MatIconModule],
  templateUrl: './car-add.html',
  styleUrl: './car-add.css',
})
export class CarAdd implements OnInit {

  car = { make: '', model: '', modelYear: new Date().getFullYear() };
  dealers: any[] = [];
  selectedDealerId: number = 0;
  message: string = '';
  error: string = '';

  constructor(
    private carService: CarService,
    private dealerService: DealerService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadDealers();
  }

  loadDealers() {
    this.dealerService.getAll().subscribe(data => { this.dealers = data; });
  }

  onSubmit() {
    if (this.selectedDealerId === 0) {
      this.error = 'Please select a dealer';
      return;
    }
    this.carService.addCar(this.car, this.selectedDealerId).subscribe({
      next: () => {
        this.message = 'Car added successfully!';
        this.error = '';
        this.clearForm();
        setTimeout(() => this.router.navigate(['/cars']), 1000);
      },
      error: () => {
        this.error = 'Failed to add car. Please try again.';
        this.message = '';
      }
    });
  }

  clearForm() {
    this.car = { make: '', model: '', modelYear: new Date().getFullYear() };
    this.selectedDealerId = 0;
  }

  resetForm() {
    this.clearForm();
    this.message = '';
    this.error = '';
  }
}
