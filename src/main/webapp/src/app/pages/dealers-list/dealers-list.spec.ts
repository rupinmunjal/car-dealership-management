import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DealersList } from './dealers-list';

describe('DealersList', () => {
  let component: DealersList;
  let fixture: ComponentFixture<DealersList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DealersList]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DealersList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
