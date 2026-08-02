import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { DealerAdd } from './dealer-add';

describe('DealerAdd', () => {
  let component: DealerAdd;
  let fixture: ComponentFixture<DealerAdd>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DealerAdd],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()],
    })
    .compileComponents();

    fixture = TestBed.createComponent(DealerAdd);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
