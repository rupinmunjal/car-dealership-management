import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { CarAdd } from './car-add';
import { AuthService } from '../../services/auth';
import { CarService } from '../../services/car';
import { DealerService } from '../../services/dealer';

describe('CarAdd', () => {
  let component: CarAdd;
  let fixture: ComponentFixture<CarAdd>;
  let siteAdmin: boolean;
  let dealerId: number | null;
  let carService: { addCar: ReturnType<typeof vi.fn> };
  let dealerService: { getAll: ReturnType<typeof vi.fn> };
  let authService: {
    isSiteAdmin: ReturnType<typeof vi.fn>;
    getDealerId: ReturnType<typeof vi.fn>;
    getDashboardPath: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    siteAdmin = false;
    dealerId = 17;
    carService = { addCar: vi.fn(() => of({ id: 1 })) };
    dealerService = {
      getAll: vi.fn(() => of([{ id: 9, name: 'Downtown Motors', location: 'Toronto' }]))
    };
    authService = {
      isSiteAdmin: vi.fn(() => siteAdmin),
      getDealerId: vi.fn(() => dealerId),
      getDashboardPath: vi.fn(() => '/dashboard/dealer-admin')
    };

    await TestBed.configureTestingModule({
      imports: [CarAdd],
      providers: [
        provideRouter([]),
        { provide: CarService, useValue: carService },
        { provide: DealerService, useValue: dealerService },
        { provide: AuthService, useValue: authService },
      ],
    })
    .compileComponents();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  function createComponent(): void {
    fixture = TestBed.createComponent(CarAdd);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it('should create', () => {
    createComponent();
    expect(component).toBeTruthy();
  });

  it('uses the authenticated dealer and hides dealer selection for dealer users', () => {
    createComponent();

    expect(component.selectedDealerId).toBe(17);
    expect(dealerService.getAll).not.toHaveBeenCalled();
    expect(fixture.nativeElement.textContent).not.toContain('Assign to Dealer');
    expect(fixture.nativeElement.textContent).not.toContain('Select a dealer');
  });

  it('loads and displays dealer selection for site admins', () => {
    siteAdmin = true;
    dealerId = null;
    createComponent();

    expect(dealerService.getAll).toHaveBeenCalledOnce();
    expect(fixture.nativeElement.textContent).toContain('Assign to Dealer');
  });

  it('submits a dealer admin car with the authenticated dealer ID', () => {
    createComponent();
    component.car = { make: 'Honda', model: 'Civic', modelYear: 2026 };

    component.onSubmit();

    expect(carService.addCar).toHaveBeenCalledWith(
      { make: 'Honda', model: 'Civic', modelYear: 2026 },
      17
    );
    expect(component.message).toBe('Car added successfully!');
    expect(component.selectedDealerId).toBe(17);
  });

  it('submits a site admin car with the selected dealer ID', () => {
    siteAdmin = true;
    dealerId = null;
    createComponent();
    component.selectedDealerId = 9;
    component.car = { make: 'Toyota', model: 'Camry', modelYear: 2025 };

    component.onSubmit();

    expect(carService.addCar).toHaveBeenCalledWith(
      { make: 'Toyota', model: 'Camry', modelYear: 2025 },
      9
    );
  });

  it('shows the API error and restores the submit state', () => {
    carService.addCar.mockReturnValue(throwError(() => ({
      error: { message: 'Subscription listing limit reached' }
    })));
    createComponent();

    component.onSubmit();

    expect(component.error).toBe('Subscription listing limit reached');
    expect(component.submitting).toBe(false);
  });

  it('does not submit a dealer user without an assigned dealer', () => {
    dealerId = null;
    createComponent();

    component.onSubmit();

    expect(carService.addCar).not.toHaveBeenCalled();
    expect(component.error).toBe('Your account is not assigned to a dealer');
  });

  it('navigates to the inventory after a successful submission', () => {
    vi.useFakeTimers();
    createComponent();
    const router = TestBed.inject(Router);
    const navigate = vi.spyOn(router, 'navigate');

    component.onSubmit();
    vi.advanceTimersByTime(1000);

    expect(navigate).toHaveBeenCalledWith(['/cars']);
  });
});
