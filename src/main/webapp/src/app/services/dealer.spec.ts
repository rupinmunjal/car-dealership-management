import { TestBed } from '@angular/core/testing';

import { Dealer } from './dealer';

describe('Dealer', () => {
  let service: Dealer;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Dealer);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
