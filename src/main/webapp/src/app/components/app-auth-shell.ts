import { Component } from '@angular/core';

/**
 * Split-screen auth layout (form left, dark brand panel right).
 * Pages project their wordmark + heading + form into the left panel via <ng-content>.
 */
@Component({
  selector: 'app-auth-shell',
  standalone: true,
  imports: [],
  template: `
    <div class="auth">
      <!-- Left: form panel -->
      <div class="form-panel">
        <div class="form-inner">
          <ng-content/>
        </div>
      </div>

      <!-- Right: brand panel -->
      <div class="brand-panel">
        <div class="dot-grid"></div>
        <div class="glow glow-a"></div>
        <div class="glow glow-b"></div>
        <div class="brand-inner">
          <div class="logo">
            <svg class="logo-svg" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M13 10V3L4 14h7v7l9-11h-7z"/>
            </svg>
          </div>
          <h2>AutoDealer Pro</h2>
          <p class="tagline">
            Manage your dealership, track inventory, and empower your team — all in one platform.
          </p>
          <div class="divider"></div>
          <div class="features">
            <div class="feature">
              <span class="check">
                <svg fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                </svg>
              </span>
              <span>Multi-role access control</span>
            </div>
            <div class="feature">
              <span class="check">
                <svg fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                </svg>
              </span>
              <span>Real-time inventory management</span>
            </div>
            <div class="feature">
              <span class="check">
                <svg fill="currentColor" viewBox="0 0 20 20">
                  <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
                </svg>
              </span>
              <span>Subscription-based packages</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .auth { min-height: 100vh; display: flex; background: #fff; }
    .form-panel {
      width: 100%; display: flex; align-items: center; justify-content: center;
      padding: 40px 24px;
    }
    .form-inner { width: 100%; max-width: 400px; }

    /* ── Brand panel ── */
    .brand-panel {
      display: none; width: 42%; position: relative; overflow: hidden;
      background: #0F172A; align-items: center; justify-content: center; padding: 48px;
    }
    .dot-grid {
      position: absolute; inset: 0;
      background-image: radial-gradient(circle, rgba(255,255,255,0.06) 1px, transparent 0);
      background-size: 28px 28px;
    }
    .glow { position: absolute; border-radius: 50%; filter: blur(40px); }
    .glow-a { top: -24px; right: -24px; width: 256px; height: 256px;
              background: radial-gradient(circle, rgba(0,82,255,0.2) 0%, transparent 70%); }
    .glow-b { bottom: -16px; left: -16px; width: 224px; height: 224px;
              background: radial-gradient(circle, rgba(77,124,255,0.15) 0%, transparent 70%); }

    .brand-inner { position: relative; text-align: center; max-width: 300px; }
    .logo {
      width: 80px; height: 80px; margin: 0 auto 28px; border-radius: 16px;
      display: flex; align-items: center; justify-content: center;
      background: linear-gradient(135deg, #0052FF, #4D7CFF);
      box-shadow: 0 12px 32px rgba(0,82,255,0.4);
    }
    .logo-svg { width: 40px; height: 40px; color: #fff; }
    h2 { font-family: 'Inter', system-ui, sans-serif; font-size: 24px; font-weight: 700; color: #fff; margin: 0 0 12px; }
    .tagline { font-size: 14px; line-height: 1.6; color: rgba(255,255,255,0.55); margin: 0; }
    .divider {
      width: 48px; height: 1px; margin: 32px auto 0;
      background: linear-gradient(to right, transparent, rgba(77,124,255,0.5), transparent);
    }
    .features { margin-top: 24px; display: flex; flex-direction: column; gap: 12px; text-align: left; }
    .feature { display: flex; align-items: center; gap: 12px; font-size: 12px; color: rgba(255,255,255,0.6); }
    .check {
      width: 20px; height: 20px; border-radius: 5px; flex-shrink: 0;
      background: rgba(0,82,255,0.3); display: flex; align-items: center; justify-content: center;
    }
    .check svg { width: 12px; height: 12px; color: #4D7CFF; }

    @media (min-width: 1024px) {
      .form-panel { width: 58%; }
      .brand-panel { display: flex; }
    }
  `],
})
export class AuthShell {}
