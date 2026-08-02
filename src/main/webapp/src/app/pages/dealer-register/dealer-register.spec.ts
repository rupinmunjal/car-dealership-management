import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { DealerRegister } from './dealer-register';

describe('DealerRegister', () => {
  let fixture: ComponentFixture<DealerRegister>;
  let component: DealerRegister;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DealerRegister],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(DealerRegister);
    component = fixture.componentInstance;
    fixture.detectChanges();
    http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/v1/packages').flush([{
      id: 1,
      name: 'Basic',
      maxEmployeeSeats: 5,
      maxCarListings: 25
    }]);
    await fixture.whenStable();
  });

  afterEach(() => http.verify());

  it('clears submitted and invalid visual state after successful registration', async () => {
    component.form = {
      name: 'Downtown Motors',
      location: 'Toronto',
      adminEmail: 'admin@downtown.test',
      adminPassword: 'password',
      packageId: 1,
      displayName: '',
      description: ''
    };
    fixture.detectChanges(false);

    component.dealerForm?.onSubmit(new Event('submit'));
    http.expectOne('/api/v1/dealers/register').flush({ id: 1 });
    await fixture.whenStable();

    expect(component.message).toBe('Dealer registered successfully!');
    expect(component.dealerForm?.submitted).toBe(false);
    expect(component.dealerForm?.pristine).toBe(true);
    expect(component.dealerForm?.untouched).toBe(true);
    expect(fixture.nativeElement.querySelectorAll('.mat-form-field-invalid').length).toBe(0);
  });
});
