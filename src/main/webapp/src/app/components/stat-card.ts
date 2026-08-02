import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

export type StatColor = 'blue' | 'green' | 'purple' | 'amber' | 'red';

const GRADIENTS: Record<StatColor, string> = {
  blue:   'linear-gradient(135deg, #0052FF, #4D7CFF)',
  green:  'linear-gradient(135deg, #0052FF, #4D7CFF)',
  purple: 'linear-gradient(135deg, #0052FF, #4D7CFF)',
  amber:  'linear-gradient(135deg, #0052FF, #4D7CFF)',
  red:    'linear-gradient(135deg, #0052FF, #4D7CFF)',
};

@Component({
  selector: 'app-stat-card',
  standalone: true,
  imports: [CommonModule, MatIconModule],
  template: `
    <div class="stat-card">
      <div class="icon-wrap" [style.background]="gradient">
        <mat-icon>{{ icon }}</mat-icon>
      </div>
      <div class="body">
        <span class="label">{{ label }}</span>
        <span class="value">{{ value }}</span>
        @if (sub) { <span class="sub" [innerHTML]="sub"></span> }
      </div>
    </div>
  `,
  styles: [`
    .stat-card {
      background: #fff; border-radius: 12px; border: 1px solid #e8edf2;
      box-shadow: 0 1px 3px rgba(0,0,0,0.06); padding: 20px;
      display: flex; align-items: center; gap: 16px;
      transition: box-shadow 0.2s, transform 0.2s;
      height: 100%;
    }
    .stat-card:hover { box-shadow: 0 4px 6px -1px rgba(0,0,0,0.07); transform: translateY(-2px); }
    .icon-wrap {
      width: 44px; height: 44px; border-radius: 12px; flex-shrink: 0;
      display: flex; align-items: center; justify-content: center; color: #fff;
      box-shadow: 0 4px 14px rgba(0,82,255,0.2);
      mat-icon { font-size: 20px; width: 20px; height: 20px; }
    }
    .body { display: flex; flex-direction: column; min-width: 0; }
    .label {
      font-family: 'Inter', system-ui, sans-serif; font-size: 10px;
      text-transform: uppercase; letter-spacing: 0.08em; color: #94a3b8; font-weight: 700;
    }
    .value {
      font-size: 24px; font-weight: 700; color: #0F172A;
      line-height: 1.2; letter-spacing: -0.3px; margin-top: 2px;
      overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
    }
    .sub { font-size: 12px; color: #94a3b8; margin-top: 2px; }
  `],
})
export class StatCard {
  @Input() icon = '';
  @Input() color: StatColor = 'blue';
  @Input() label = '';
  @Input() value: string | number = '';
  @Input() sub?: string;

  get gradient() { return GRADIENTS[this.color]; }
}
