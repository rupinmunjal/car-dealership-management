import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CarService } from '../../services/car';
import { AuthService } from '../../services/auth';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-cars-list',
  imports: [CommonModule, RouterModule],
  templateUrl: './cars-list.html',
  styleUrls: ['./cars-list.css'],
})
export class CarsList implements OnInit {

  cars: any[] = [];
  uniqueBrandsCount: number = 0;

  constructor(
    private carService: CarService,
    private authService: AuthService,
    private cd: ChangeDetectorRef
  ) {}

  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  ngOnInit(): void {
    this.loadCars();
    this.loadBrandsCount();
  }

  loadCars() {
    this.carService.getAll().subscribe({
      next: (data) => {
        this.cars = data;
        this.cd.detectChanges(); // update view
      },
      error: () => {
        this.cars = [];
        this.cd.detectChanges(); // update view
      }
    });
  }

  loadBrandsCount() {
    this.carService.getUniqueBrandsCount().subscribe({
      next: (data) => {
        this.uniqueBrandsCount = data.uniqueBrandsCount;
        this.cd.detectChanges();
      },
      error: () => {
        this.uniqueBrandsCount = 0;
        this.cd.detectChanges();
      }
    });
  }

  deleteCar(id: number) {
    if (confirm("Are you sure you want to delete this car?")) {
      this.carService.deleteCar(id).subscribe({
        next: () => {
          this.loadCars();
          this.loadBrandsCount();
        },
        error: () => {}
      });
    }
  }
}
