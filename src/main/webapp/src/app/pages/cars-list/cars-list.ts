import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CarService } from '../../services/car';
import { AuthService } from '../../services/auth';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { FormsModule } from '@angular/forms';
import { AppAvatar, AppButton, DataTable, PageHeader, StatCard } from '../../components';

@Component({
  selector: 'app-cars-list',
  imports: [CommonModule, FormsModule, MatIconModule, MatPaginatorModule, MatChipsModule,
    MatFormFieldModule, MatInputModule, MatSelectModule,
    PageHeader, StatCard, AppButton, DataTable, AppAvatar],
  templateUrl: './cars-list.html',
})
export class CarsList implements OnInit {

  cars: any[] = [];
  uniqueBrandsCount: number = 0;
  totalElements = 0;
  pageIndex = 0;
  pageSize = 20;
  sort = 'id,asc';
  filters: {
    search: string;
    make: string;
    model: string;
    year: number | null;
    minPrice: number | null;
    maxPrice: number | null;
  } = { search: '', make: '', model: '', year: null, minPrice: null, maxPrice: null };

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
    this.carService.getPage({
      ...this.filters,
      page: this.pageIndex,
      size: this.pageSize,
      sort: this.sort
    }).subscribe({
      next: (data) => {
        this.cars = data.content;
        this.totalElements = data.totalElements;
        this.cd.detectChanges();
      },
      error: () => { this.cars = []; this.totalElements = 0; this.cd.detectChanges(); }
    });
  }

  applyFilters(): void {
    this.pageIndex = 0;
    this.loadCars();
  }

  clearFilters(): void {
    this.filters = { search: '', make: '', model: '', year: null, minPrice: null, maxPrice: null };
    this.applyFilters();
  }

  clearFilter(key: keyof CarsList['filters']): void {
    this.filters[key] = (typeof this.filters[key] === 'string' ? '' : null) as never;
    this.applyFilters();
  }

  pageChanged(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadCars();
  }

  get activeFilters(): Array<{ key: keyof CarsList['filters']; label: string }> {
    const labels: Record<keyof CarsList['filters'], string> = {
      search: 'Search', make: 'Make', model: 'Model', year: 'Year',
      minPrice: 'Min price', maxPrice: 'Max price'
    };
    return (Object.keys(this.filters) as Array<keyof CarsList['filters']>)
      .filter(key => this.filters[key] !== null && String(this.filters[key]).trim() !== '')
      .map(key => ({ key, label: `${labels[key]}: ${this.filters[key]}` }));
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
