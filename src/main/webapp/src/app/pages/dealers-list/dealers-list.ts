import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { DealerService } from '../../services/dealer';
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
  selector: 'app-dealers-list',
  imports: [CommonModule, RouterModule,
    MatTableModule, MatCardModule, MatButtonModule,
    MatIconModule, MatChipsModule, MatTooltipModule],
  templateUrl: './dealers-list.html',
  styleUrl: './dealers-list.css',
})
export class DealersListComponent implements OnInit {

  dealers: any[] = [];
  totalInventory: number = 0;

  constructor(
    private dealerService: DealerService,
    private authService: AuthService,
    private cd: ChangeDetectorRef
  ) {}

  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  get displayedColumns(): string[] {
    return this.isAdmin
      ? ['id', 'name', 'location', 'cars', 'actions']
      : ['id', 'name', 'location', 'cars'];
  }

  ngOnInit(): void {
    this.loadDealers();
    this.loadInventoryStats();
  }

  loadDealers() {
    this.dealerService.getAll().subscribe({
      next: (data) => { this.dealers = data; this.cd.detectChanges(); },
      error: () => { this.dealers = []; this.cd.detectChanges(); }
    });
  }

  loadInventoryStats() {
    this.dealerService.getInventoryStats().subscribe({
      next: (data) => { this.totalInventory = data.totalInventory; this.cd.detectChanges(); },
      error: () => { this.totalInventory = 0; this.cd.detectChanges(); }
    });
  }

  deleteDealer(id: number) {
    if (confirm('Are you sure you want to delete this dealer?')) {
      this.dealerService.deleteDealer(id).subscribe({
        next: () => { this.loadDealers(); this.loadInventoryStats(); },
        error: () => {}
      });
    }
  }
}
