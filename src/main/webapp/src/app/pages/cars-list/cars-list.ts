import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CarService } from '../../services/car';
import { AuthService } from '../../services/auth';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { AppAvatar, AppButton, DataTable, PageHeader, StatCard } from '../../components';

@Component({
  selector: 'app-cars-list',
  imports: [CommonModule, MatIconModule,
    PageHeader, StatCard, AppButton, DataTable, AppAvatar],
  templateUrl: './cars-list.html',
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
      next: (data) => { this.cars = data; this.cd.detectChanges(); },
      error: () => { this.cars = []; this.cd.detectChanges(); }
    });
  }

  loadBrandsCount() {
    this.carService.getUniqueBrandsCount().subscribe({
      next: (data) => { this.uniqueBrandsCount = data.uniqueBrandsCount; this.cd.detectChanges(); },
      error: () => { this.uniqueBrandsCount = 0; this.cd.detectChanges(); }
    });
  }

  deleteCar(id: number) {
    if (confirm('Are you sure you want to delete this car?')) {
      this.carService.deleteCar(id).subscribe({
        next: () => { this.loadCars(); this.loadBrandsCount(); },
        error: () => {}
      });
    }
  }
}
