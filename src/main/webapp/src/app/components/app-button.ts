import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

type Variant = 'primary' | 'outline' | 'ghost' | 'danger';

@Component({
  selector: 'app-btn',
  standalone: true,
  imports: [CommonModule, RouterModule, MatIconModule, MatProgressSpinnerModule],
  template: `
    <ng-template #content>
      @if (loading) { <mat-spinner diameter="16" class="spinner"/> }
      @else if (icon) { <mat-icon class="icon">{{ icon }}</mat-icon> }
      <ng-content/>
    </ng-template>

    @if (routerLink) {
      <a [routerLink]="routerLink" [class]="cls" [attr.aria-label]="ariaLabel" [attr.title]="title">
        <ng-container [ngTemplateOutlet]="content"/>
      </a>
    } @else {
      <button [type]="type" [disabled]="disabled || loading" [class]="cls" (click)="clicked.emit()"
              [attr.aria-label]="ariaLabel" [attr.title]="title">
        <ng-container [ngTemplateOutlet]="content"/>
      </button>
    }
  `,
  styles: [`
    :host { display: inline-block; }
    :host(.w-full) { display: block; }
    :host(.w-full) a, :host(.w-full) button { width: 100%; justify-content: center; }
    a, button {
      display: inline-flex; align-items: center; gap: 6px;
      min-height: 44px; padding: 10px 18px; font-size: 14px; font-weight: 600;
      border-radius: 10px; cursor: pointer; text-decoration: none;
      border: none; transition: all 0.2s cubic-bezier(0.4,0,0.2,1);
      white-space: nowrap;
    }
    .icon { font-size: 16px; width: 16px; height: 16px; }
    .spinner { display: inline-block; }
    button:disabled { opacity: 0.6; cursor: not-allowed; }
    a:focus-visible, button:focus-visible {
      outline: 2px solid var(--ring); outline-offset: 2px;
    }

    .btn-primary {
      background: var(--gradient-brand); color: #fff;
      box-shadow: 0 4px 14px rgba(0,82,255,0.25);
    }
    .btn-primary:hover:not(:disabled) { filter: brightness(1.07); transform: translateY(-1px); box-shadow: 0 8px 24px rgba(0,82,255,0.35); }
    .btn-primary:active:not(:disabled) { transform: scale(0.98); }

    .btn-outline {
      background: #fff; color: #374151;
      border: 1px solid #e8edf2; box-shadow: 0 1px 2px rgba(0,0,0,0.04);
    }
    .btn-outline:hover { background: #f8fafc; border-color: #c8d0dc; }

    .btn-ghost { background: transparent; color: #64748b; }
    .btn-ghost:hover { background: #f1f5f9; color: #0F172A; }

    .btn-danger { background: transparent; color: #94a3b8; padding: 6px; border-radius: 8px; }
    .btn-danger:hover { color: #dc2626; background: #fef2f2; }
  `],
})
export class AppButton {
  @Input() variant: Variant = 'primary';
  @Input() icon?: string;
  @Input() routerLink?: string;
  @Input() type: 'button' | 'submit' = 'button';
  @Input() disabled = false;
  @Input() loading = false;
  @Input() ariaLabel?: string;
  @Input() title?: string;
  @Output() clicked = new EventEmitter<void>();

  get cls() { return `btn-${this.variant}`; }
}
