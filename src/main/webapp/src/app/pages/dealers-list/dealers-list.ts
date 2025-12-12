import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { DealerService } from '../../services/dealer';
import { AuthService } from '../../services/auth';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-dealers-list',
  imports: [CommonModule, RouterModule],
  templateUrl: './dealers-list.html'
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
      next: (data) => {
        this.dealers = data;
        this.cd.detectChanges(); // update view
      },
      error: () => {
        this.dealers = [];
        this.cd.detectChanges(); // update view
      }
    });
  }

  loadInventoryStats() {
    this.dealerService.getInventoryStats().subscribe({
      next: (data) => {
        this.totalInventory = data.totalInventory;
        this.cd.detectChanges();
      },
      error: () => {
        this.totalInventory = 0;
        this.cd.detectChanges();
      }
    });
  }

  deleteDealer(id: number) {
    if (confirm("Are you sure?")) {
      this.dealerService.deleteDealer(id).subscribe({
        next: () => {
          this.loadDealers();
          this.loadInventoryStats();
        },
        error: () => {}
      });
    }
  }
}
