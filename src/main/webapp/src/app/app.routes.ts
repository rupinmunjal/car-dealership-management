import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { Login } from './pages/login/login';
import { Register } from './pages/register/register';
import { CarsList } from './pages/cars-list/cars-list';
import { CarAdd } from './pages/car-add/car-add';
import { DealersListComponent } from './pages/dealers-list/dealers-list';
import { DealerAdd } from './pages/dealer-add/dealer-add';
import { SiteAdminDashboard } from './pages/site-admin-dashboard/site-admin-dashboard';
import { DealerAdminDashboard } from './pages/dealer-admin-dashboard/dealer-admin-dashboard';
import { DealerEmployeeDashboard } from './pages/dealer-employee-dashboard/dealer-employee-dashboard';
import { PackageManagement } from './pages/package-management/package-management';
import { DealerRegister } from './pages/dealer-register/dealer-register';
import { DealerSettings } from './pages/dealer-settings/dealer-settings';
import { EmployeeManagement } from './pages/employee-management/employee-management';
import { authGuard, publicOnlyGuard, roleGuard } from './guards/auth.guard';

export const routes: Routes = [
  // ── Public routes ──────────────────────────────────────────
  { path: 'login', component: Login, canActivate: [publicOnlyGuard] },
  { path: 'register', redirectTo: 'login' },

  // ── Role dashboard routes ──────────────────────────────────
  {
    path: 'dashboard/site-admin',
    component: SiteAdminDashboard,
    canActivate: [authGuard, roleGuard('SITE_ADMIN')],
    children: [],
  },
  {
    path: 'dashboard/dealer-admin',
    component: DealerAdminDashboard,
    canActivate: [authGuard, roleGuard('DEALER_ADMIN')],
    children: [],
  },
  {
    path: 'dashboard/dealer-employee',
    component: DealerEmployeeDashboard,
    canActivate: [authGuard, roleGuard('DEALER_EMPLOYEE')],
    children: [],
  },

  // ── SITE_ADMIN routes ──────────────────────────────────────
  { path: 'dealers', component: DealersListComponent, canActivate: [authGuard, roleGuard('SITE_ADMIN')] },
  { path: 'dealers/add', component: DealerAdd, canActivate: [authGuard, roleGuard('SITE_ADMIN')] },
  { path: 'dealers/register', component: DealerRegister, canActivate: [authGuard, roleGuard('SITE_ADMIN')] },
  { path: 'packages', component: PackageManagement, canActivate: [authGuard, roleGuard('SITE_ADMIN')] },

  // ── DEALER_ADMIN routes ────────────────────────────────────
  { path: 'dealers/settings', component: DealerSettings, canActivate: [authGuard, roleGuard('DEALER_ADMIN')] },
  { path: 'dealers/employees', component: EmployeeManagement, canActivate: [authGuard, roleGuard('DEALER_ADMIN')] },

  // ── Authenticated routes (all roles) ───────────────────────
  { path: '', component: Home, canActivate: [authGuard] },
  { path: 'cars', component: CarsList, canActivate: [authGuard] },
  { path: 'cars/add', component: CarAdd, canActivate: [authGuard] },

  // ── Catch-all ──────────────────────────────────────────────
  { path: '**', redirectTo: '' },
];
