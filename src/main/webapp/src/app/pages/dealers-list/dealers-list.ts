import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { DealerService } from '../../services/dealer';
import { AuthService } from '../../services/auth';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { AppAvatar, AppButton, DataTable, PageHeader, StatCard } from '../../components';

@Component({
  selector: 'app-dealers-list',
  imports: [CommonModule, MatIconModule,
    PageHeader, StatCard, AppButton, DataTable, AppAvatar],
  templateUrl: './dealers-list.html',
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
