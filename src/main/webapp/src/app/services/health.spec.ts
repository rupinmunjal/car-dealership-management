import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Health, HealthResponse } from './health';

describe('Health', () => {
  let service: Health;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [Health]
    });
    service = TestBed.inject(Health);
    httpMock = TestBed.inject(HttpTestingController);
    httpMock.expectOne('/actuator/health').flush({ status: 'UP' });
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should check health and update status to UP', () => {
    const mockHealthResponse: HealthResponse = { status: 'UP' };

    service.checkHealth().subscribe();

    const req = httpMock.expectOne('/actuator/health');
    expect(req.request.method).toBe('GET');
    req.flush(mockHealthResponse);

    expect(service.getHealthStatus()).toBe('UP');
  });

  it('should check health and update status to DOWN on error', () => {
    service.checkHealth().subscribe({
      error: () => {
        // Expected error
      }
    });

    const req = httpMock.expectOne('/actuator/health');
    expect(req.request.method).toBe('GET');
    req.error(new ProgressEvent('error'));

    expect(service.getHealthStatus()).toBe('DOWN');
  });

  it('should return current health status', () => {
    const status = service.getHealthStatus();
    expect(status).toBeDefined();
    expect(typeof status).toBe('string');
  });

  it('should have healthStatus$ observable', () => {
    expect(service.healthStatus$).toBeDefined();
    service.healthStatus$.subscribe(status => {
      expect(typeof status).toBe('string');
    });
  });
});
