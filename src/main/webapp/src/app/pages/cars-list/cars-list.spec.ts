import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CarsList } from './cars-list';

describe('CarsList', () => {
  let component: CarsList;
  let fixture: ComponentFixture<CarsList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CarsList]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CarsList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
