import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService, UserRole } from '../services/auth';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    return true;
  }
  router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
  return false;
};

export const publicOnlyGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isLoggedIn()) {
    return true;
  }
  router.navigate([authService.getDashboardPath()]);
  return false;
};

export const roleGuard = (...allowedRoles: UserRole[]): CanActivateFn => {
  return (route, state) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isLoggedIn()) {
      router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
      return false;
    }

    const role = authService.getRole();
    if (role && allowedRoles.includes(role)) {
      if (authService.isSuspended() && role !== 'SITE_ADMIN') {
        router.navigate(['/login']);
        return false;
      }
      return true;
    }

    router.navigate([authService.getDashboardPath()]);
    return false;
  };
};
