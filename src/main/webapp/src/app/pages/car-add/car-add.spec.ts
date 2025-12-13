import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CarAdd } from './car-add';

describe('CarAdd', () => {
  let component: CarAdd;
  let fixture: ComponentFixture<CarAdd>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CarAdd]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CarAdd);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
