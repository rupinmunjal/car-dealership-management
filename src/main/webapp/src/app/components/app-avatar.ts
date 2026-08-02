import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

export type AvatarColor = 'blue' | 'purple' | 'green' | 'amber' | 'red' | 'gray';

const GRADIENTS: Record<AvatarColor, string> = {
  blue:   'linear-gradient(135deg, #0052FF, #4D7CFF)',
  purple: 'linear-gradient(135deg, #7c3aed, #a78bfa)',
  green:  'linear-gradient(135deg, #16a34a, #4ade80)',
  amber:  'linear-gradient(135deg, #d97706, #fbbf24)',
  red:    'linear-gradient(135deg, #dc2626, #f87171)',
  gray:   'linear-gradient(135deg, #64748b, #94a3b8)',
};

/** Initials circle used in table rows. Renders the first character of `name`. */
@Component({
  selector: 'app-avatar',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="avatar" [style.background]="gradient" [style.width.px]="size"
         [style.height.px]="size" [style.fontSize.px]="size * 0.4">
      {{ initial }}
    </div>
  `,
  styles: [`
    .avatar {
      border-radius: 50%; display: flex; align-items: center; justify-content: center;
      color: #fff; font-weight: 700; flex-shrink: 0; line-height: 1;
    }
  `],
})
export class AppAvatar {
  @Input() name = '';
  @Input() color: AvatarColor = 'blue';
  @Input() size = 32;

  get initial() { return this.name ? this.name.charAt(0).toUpperCase() : '?'; }
  get gradient() { return GRADIENTS[this.color]; }
}
