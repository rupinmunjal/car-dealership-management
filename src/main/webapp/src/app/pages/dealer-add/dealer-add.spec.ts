import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DealerAdd } from './dealer-add';

describe('DealerAdd', () => {
  let component: DealerAdd;
  let fixture: ComponentFixture<DealerAdd>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DealerAdd]
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
