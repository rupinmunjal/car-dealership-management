import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { DealerService } from '../../services/dealer';
import { AuthService } from '../../services/auth';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { AppAvatar, AppButton, DataTable, PageHeader, StatCard } from '../../components';

@Component({
  selector: 'app-dealers-list',
  imports: [CommonModule, MatIconModule, MatPaginatorModule,
    PageHeader, StatCard, AppButton, DataTable, AppAvatar],
  templateUrl: './dealers-list.html',
})
export class DealersListComponent implements OnInit {

  dealers: any[] = [];
  totalInventory: number = 0;
  totalDealers = 0;
  pageIndex = 0;
  pageSize = 20;

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
    this.dealerService.getPage(this.pageIndex, this.pageSize).subscribe({
      next: (data) => {
        this.dealers = data.content;
        this.totalDealers = data.totalElements;
        this.cd.detectChanges();
      },
      error: () => { this.dealers = []; this.totalDealers = 0; this.cd.detectChanges(); }
    });
  }

  pageChanged(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadDealers();
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
