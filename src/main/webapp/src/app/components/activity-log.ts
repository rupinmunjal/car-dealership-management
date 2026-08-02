import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-activity-log',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatPaginatorModule, MatIconModule],
  template: `
    <div class="overflow-hidden rounded-xl border border-slate-200 bg-white">
      <div class="overflow-x-auto">
        <table mat-table [dataSource]="rows" class="w-full">
          <ng-container matColumnDef="timestamp">
            <th mat-header-cell *matHeaderCellDef>Time</th>
            <td mat-cell *matCellDef="let row" class="whitespace-nowrap text-sm text-slate-500">
              {{ row.timestamp | date:'medium' }}
            </td>
          </ng-container>

          <ng-container matColumnDef="action">
            <th mat-header-cell *matHeaderCellDef>Action</th>
            <td mat-cell *matCellDef="let row">
              <span class="chip chip-brand">{{ actionLabel(row.action) }}</span>
            </td>
          </ng-container>

          <ng-container matColumnDef="entity">
            <th mat-header-cell *matHeaderCellDef>Record</th>
            <td mat-cell *matCellDef="let row" class="text-sm font-medium text-slate-700">
              {{ row.entityType }}<span *ngIf="row.entityId"> #{{ row.entityId }}</span>
            </td>
          </ng-container>

          <ng-container matColumnDef="actor">
            <th mat-header-cell *matHeaderCellDef>Actor</th>
            <td mat-cell *matCellDef="let row" class="text-sm text-slate-600">
              {{ row.actorEmail || 'System' }}
            </td>
          </ng-container>

          <ng-container matColumnDef="details">
            <th mat-header-cell *matHeaderCellDef>Details</th>
            <td mat-cell *matCellDef="let row" class="max-w-md">
              <span class="block truncate font-mono text-xs text-slate-500" [title]="details(row.details)">
                {{ details(row.details) }}
              </span>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="columns"></tr>
          <tr mat-row *matRowDef="let row; columns: columns"></tr>
        </table>

        <div *ngIf="rows.length === 0" class="px-6 py-12 text-center text-sm text-slate-500">
          <mat-icon class="mb-2 text-slate-300">history</mat-icon>
          <p>No recorded activity yet.</p>
        </div>
      </div>

      <mat-paginator [length]="totalElements" [pageIndex]="pageIndex" [pageSize]="pageSize"
                     [pageSizeOptions]="[10, 20, 50]" showFirstLastButtons
                     (page)="pageChanged($event)" aria-label="Select activity log page"/>
    </div>
  `,
})
export class ActivityLog implements OnInit {
  rows: any[] = [];
  columns = ['timestamp', 'action', 'entity', 'actor', 'details'];
  totalElements = 0;
  pageIndex = 0;
  pageSize = 20;

  constructor(private http: HttpClient, private cd: ChangeDetectorRef) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.http.get<any>('/api/v1/audit-logs', {
      params: {
        page: this.pageIndex,
        size: this.pageSize,
        sort: 'timestamp,desc'
      }
    }).subscribe({
      next: response => {
        this.rows = response.content ?? [];
        this.totalElements = response.totalElements ?? this.rows.length;
        this.cd.detectChanges();
      },
      error: () => {
        this.rows = [];
        this.totalElements = 0;
        this.cd.detectChanges();
      }
    });
  }

  pageChanged(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.load();
  }

  actionLabel(action: string): string {
    return (action || '').toLowerCase().replaceAll('_', ' ');
  }

  details(value: unknown): string {
    if (value == null) return 'No additional details';
    return typeof value === 'string' ? value : JSON.stringify(value);
  }
}
