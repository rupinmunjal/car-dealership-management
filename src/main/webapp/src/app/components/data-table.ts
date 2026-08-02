import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { AppButton } from './app-button';

/**
 * Table card with built-in empty state. `emptyActionLink` renders a router-link
 * button; `emptyActionClick` (output) renders a clickable button (e.g. to open a dialog).
 */
@Component({
  selector: 'app-data-table',
  standalone: true,
  imports: [CommonModule, MatIconModule, AppButton],
  template: `
    <div class="table-shell">
      <div class="overflow-x-auto">
        @if (rows.length > 0) {
          <table [class.table-fixed]="fixedLayout">
            <ng-content/>
          </table>
        } @else {
          <div class="empty">
            <div class="empty-icon-wrap"><mat-icon>{{ emptyIcon }}</mat-icon></div>
            <h3>{{ emptyTitle }}</h3>
            <p>{{ emptyText }}</p>
            @if (!emptyActionHidden) {
              <app-btn *ngIf="emptyActionLink && emptyActionLabel"
                       [routerLink]="emptyActionLink">{{ emptyActionLabel }}</app-btn>
              <app-btn *ngIf="!emptyActionLink && emptyActionLabel"
                       (clicked)="emptyActionClick.emit()">{{ emptyActionLabel }}</app-btn>
            }
          </div>
        }
      </div>
    </div>
  `,
  styles: [`
    .overflow-x-auto { overflow-x: auto; }
    .empty { text-align: center; padding: 56px 24px; }
    .empty-icon-wrap {
      width: 56px; height: 56px; margin: 0 auto 14px; border-radius: 16px;
      background: #f1f5f9; display: flex; align-items: center; justify-content: center;
      mat-icon { font-size: 26px; width: 26px; height: 26px; color: #cbd5e1; }
    }
    h3 { font-size: 16px; font-weight: 600; color: #0F172A; margin: 0 0 4px; }
    p  { font-size: 14px; color: #64748b; margin: 0 0 20px; }
  `],
})
export class DataTable {
  @Input() rows: unknown[] = [];
  @Input() fixedLayout = false;
  @Input() emptyIcon = 'inbox';
  @Input() emptyTitle = 'Nothing here yet';
  @Input() emptyText = '';
  @Input() emptyActionLink?: string;
  @Input() emptyActionLabel?: string;
  @Input() emptyActionHidden = false;
  @Output() emptyActionClick = new EventEmitter<void>();
}
