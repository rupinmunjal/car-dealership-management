import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Health } from '../../services/health';
import { Subscription } from 'rxjs';
import { MatChipsModule } from '@angular/material/chips';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-health-indicator',
  imports: [CommonModule, MatChipsModule, MatIconModule],
  templateUrl: './health-indicator.html',
  styleUrl: './health-indicator.css',
})
export class HealthIndicator implements OnInit, OnDestroy {
  healthStatus: string = 'UNKNOWN';
  private subscription?: Subscription;

  constructor(private healthService: Health) {}

  ngOnInit(): void {
    this.subscription = this.healthService.healthStatus$.subscribe(
      status => { this.healthStatus = status; }
    );
  }

  ngOnDestroy(): void {
    if (this.subscription) { this.subscription.unsubscribe(); }
  }

  getStatusColor(): 'primary' | 'warn' | undefined {
    switch (this.healthStatus) {
      case 'UP': return 'primary';
      case 'DOWN': return 'warn';
      default: return undefined;
    }
  }

  getStatusIcon(): string {
    switch (this.healthStatus) {
      case 'UP': return 'check_circle';
      case 'DOWN': return 'cancel';
      default: return 'help';
    }
  }
}
