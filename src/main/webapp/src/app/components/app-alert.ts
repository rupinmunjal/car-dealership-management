import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

export type AlertType = 'success' | 'error';

/**
 * Consistent success/error banner. Renders nothing when `message` is empty,
 * so pages can bind a message variable directly.
 */
@Component({
  selector: 'app-alert',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    @if (message) {
      <div class="alert" [class.success]="type === 'success'" [class.error]="type === 'error'" role="alert">
        <mat-icon class="a-icon">{{ type === 'success' ? 'check_circle' : 'error_outline' }}</mat-icon>
        <span class="a-msg">{{ message }}</span>
        @if (dismissible) {
          <button type="button" class="a-close" (click)="dismissed.emit()" aria-label="Dismiss">
            <mat-icon>close</mat-icon>
          </button>
        }
      </div>
    }
  `,
  styles: [`
    .alert {
      display: flex; align-items: center; gap: 10px;
      padding: 12px 16px; border-radius: 10px; margin-bottom: 12px;
      font-size: 14px; font-weight: 500;
    }
    .a-icon  { font-size: 18px; width: 18px; height: 18px; flex-shrink: 0; }
    .a-msg   { flex: 1; }
    .a-close {
      background: none; border: none; cursor: pointer; padding: 2px; opacity: 0.7;
      display: flex; align-items: center; justify-content: center; color: inherit;
    }
    .a-close mat-icon { font-size: 16px; width: 16px; height: 16px; }
    .success { background: #f0fdf4; color: #15803d; border: 1px solid #bbf7d0; }
    .error   { background: #fef2f2; color: #dc2626; border: 1px solid #fecaca; }
  `],
})
export class AppAlert {
  @Input() type: AlertType = 'success';
  @Input() message = '';
  @Input() dismissible = false;
  @Output() dismissed = new EventEmitter<void>();
}
