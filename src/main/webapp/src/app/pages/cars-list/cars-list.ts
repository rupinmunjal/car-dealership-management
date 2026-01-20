import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CarService } from '../../services/car';
import { AuthService } from '../../services/auth';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-cars-list',
  imports: [CommonModule, RouterModule,
    MatTableModule, MatCardModule, MatButtonModule,
    MatIconModule, MatChipsModule, MatTooltipModule],
  templateUrl: './cars-list.html',
  styleUrl: './cars-list.css',
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

  get displayedColumns(): string[] {
    return this.isAdmin
      ? ['id', 'make', 'model', 'modelYear', 'actions']
      : ['id', 'make', 'model', 'modelYear'];
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
