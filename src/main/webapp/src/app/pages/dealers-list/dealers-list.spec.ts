import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { DealersListComponent } from './dealers-list';

describe('DealersList', () => {
  let component: DealersListComponent;
  let fixture: ComponentFixture<DealersListComponent>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DealersListComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    })
    .compileComponents();

    fixture = TestBed.createComponent(DealersListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/v1/dealers?page=0&size=20&sort=id,asc').flush({
      content: [{
        id: 1,
        name: 'Downtown Motors',
        adminEmail: 'admin@downtown.example',
        location: 'Toronto',
        cars: []
      }],
      totalElements: 1
    });
    http.expectOne('/api/v1/dealers/stats/inventory').flush({ totalInventory: 0 });
    await fixture.whenStable();
  });

  afterEach(() => http.verify());

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('shows the dealer admin email beneath the dealer name', () => {
    expect(fixture.nativeElement.textContent).toContain('Downtown Motors');
    expect(fixture.nativeElement.textContent).toContain('admin@downtown.example');
  });
});
