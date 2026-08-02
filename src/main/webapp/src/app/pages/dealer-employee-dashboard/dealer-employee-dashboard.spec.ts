import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { DealerEmployeeDashboard } from './dealer-employee-dashboard';
import { AuthService } from '../../services/auth';

describe('DealerEmployeeDashboard', () => {
  let fixture: ComponentFixture<DealerEmployeeDashboard>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DealerEmployeeDashboard],
      providers: [
        provideRouter([]),
        {
          provide: AuthService,
          useValue: {
            getEmail: () => 'employee@example.com',
            getPermissions: () => ['CAN_ADD_CAR', 'CAN_EDIT_CAR'],
            hasPermission: (permission: string) => permission === 'CAN_ADD_CAR',
            logout: vi.fn(),
          }
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DealerEmployeeDashboard);
    fixture.detectChanges();
  });

  it('renders user-facing permission wording', () => {
    const text = fixture.nativeElement.textContent;

    expect(text).toContain('Add cars');
    expect(text).toContain('Edit cars');
    expect(text).toContain('Add vehicles to your dealership inventory');
    expect(text).not.toContain('CAN_ADD_CAR');
  });
});
