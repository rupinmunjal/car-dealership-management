import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Health } from '../../services/health';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-health-indicator',
  imports: [CommonModule],
  templateUrl: './health-indicator.html',
  styleUrl: './health-indicator.css',
})
export class HealthIndicator implements OnInit, OnDestroy {
  healthStatus: string = 'UNKNOWN';
  private subscription?: Subscription;

  constructor(private healthService: Health) {}

  ngOnInit(): void {
    this.subscription = this.healthService.healthStatus$.subscribe(
      status => {
        this.healthStatus = status;
      }
    );
  }

  ngOnDestroy(): void {
    if (this.subscription) {
      this.subscription.unsubscribe();
    }
  }

  getStatusColor(): string {
    switch (this.healthStatus) {
      case 'UP':
        return 'green';
      case 'DOWN':
        return 'red';
      default:
        return 'gray';
    }
  }

  getStatusIcon(): string {
    switch (this.healthStatus) {
      case 'UP':
        return '✓';
      case 'DOWN':
        return '✗';
      default:
        return '?';
    }
  }
}
