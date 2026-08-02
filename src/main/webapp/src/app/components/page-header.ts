import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AppButton } from './app-button';
import { AuthService } from '../services/auth';

/**
 * Consistent page header: section-label pill + display heading + subtitle.
 * Projects action buttons/links into the right side via <ng-content select="[header-actions]">
 */
@Component({
  selector: 'app-page-header',
  standalone: true,
  imports: [CommonModule, AppButton],
  template: `
    <div class="ph">
      <div class="left">
        <span class="pill">{{ label }}</span>
        <h1>{{ title }}</h1>
        @if (subtitle) { <p>{{ subtitle }}</p> }
      </div>
      <div class="actions">
        <ng-content select="[header-actions]"/>
        @if (showDashboard) {
          <app-btn variant="outline" icon="dashboard" [routerLink]="dashboardLink">
            Back to Dashboard
          </app-btn>
        }
      </div>
    </div>
  `,
  styles: [`
    .ph {
      display: flex; flex-wrap: wrap; align-items: flex-start;
      justify-content: space-between; gap: 16px; margin-bottom: 24px;
    }
    .pill {
      display: inline-flex; align-items: center; gap: 8px;
      font-family: 'Inter', system-ui, sans-serif; font-size: 11px;
      text-transform: uppercase; letter-spacing: 0.12em; font-weight: 700;
      color: #0052FF; background: rgba(0,82,255,0.07);
      border: 1px solid rgba(0,82,255,0.2);
      padding: 3px 12px; border-radius: 20px; margin-bottom: 10px;
    }
    .pill::before {
      content: ''; width: 6px; height: 6px; border-radius: 50%;
      background: var(--brand); box-shadow: 0 0 0 4px rgba(0,82,255,0.1);
    }
    h1 {
      font-family: 'Inter', system-ui, sans-serif; font-size: 28px;
      font-weight: 700; letter-spacing: -0.6px; color: #0F172A;
      margin: 0 0 4px; line-height: 1.2;
    }
    p { margin: 0; font-size: 14px; line-height: 1.5; color: #64748b; }
    .actions { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
  `],
})
export class PageHeader {
  @Input() label = '';
  @Input() title = '';
  @Input() subtitle = '';
  @Input() showDashboard = true;

  constructor(private auth: AuthService) {}

  get dashboardLink(): string {
    return this.auth.getDashboardPath();
  }
}
