import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { PageHeader } from './page-header';
import { AuthService } from '../services/auth';

describe('PageHeader', () => {
  let fixture: ComponentFixture<PageHeader>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PageHeader],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: { getDashboardPath: () => '/dashboard/site-admin' } },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PageHeader);
  });

  it('shows a role-aware dashboard link by default', () => {
    fixture.detectChanges();

    const link = fixture.nativeElement.querySelector('a');
    expect(link.textContent).toContain('Back to Dashboard');
    expect(link.getAttribute('href')).toBe('/dashboard/site-admin');
  });

  it('can hide the dashboard link on dashboard pages', () => {
    fixture.componentRef.setInput('showDashboard', false);
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).not.toContain('Back to Dashboard');
  });
});
