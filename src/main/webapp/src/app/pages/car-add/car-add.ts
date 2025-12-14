import { Component, OnInit } from '@angular/core';
import { CarService } from '../../services/car';
import { DealerService } from '../../services/dealer';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-car-add',
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './car-add.html',
  styleUrl: './car-add.css',
})
export class CarAdd implements OnInit {

  car = {
    make: '',
    model: '',
    modelYear: new Date().getFullYear()
  };

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
    this.dealerService.getAll().subscribe(data => {
      this.dealers = data;
    });
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
        // Clear only the form data, not the messages
        this.clearForm();
        // Redirect after 1 second
        setTimeout(() => {
          this.router.navigate(['/cars']);
        }, 1000);
      },
      error: (err) => {
        this.error = 'Failed to add car. Please try again.';
        this.message = '';
        console.error(err);
      }
    });
  }

  clearForm() {
    this.car = {
      make: '',
      model: '',
      modelYear: new Date().getFullYear()
    };
    this.selectedDealerId = 0;
  }

  resetForm() {
    this.clearForm();
    this.message = '';
    this.error = '';
  }
}
