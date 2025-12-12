import { Routes } from '@angular/router';
import { Home } from './pages/home/home';
import { DealersListComponent } from './pages/dealers-list/dealers-list';
import { DealerAdd } from './pages/dealer-add/dealer-add';
import { CarsList } from './pages/cars-list/cars-list';
import { CarAdd } from './pages/car-add/car-add';
import { Login } from './pages/login/login';
import { Register } from './pages/register/register';
import { authGuard, publicOnlyGuard } from './guards/auth.guard';

export const routes: Routes = [
  // Public routes - only accessible when not logged in
  { path: 'login', component: Login, canActivate: [publicOnlyGuard] },
  { path: 'register', component: Register, canActivate: [publicOnlyGuard] },

  // Protected routes - require authentication
  { path: '', component: Home, canActivate: [authGuard] },
  { path: 'dealers', component: DealersListComponent, canActivate: [authGuard] },
  { path: 'dealers/add', component: DealerAdd, canActivate: [authGuard] },
  { path: 'cars', component: CarsList, canActivate: [authGuard] },
  { path: 'cars/add', component: CarAdd, canActivate: [authGuard] },

  // Redirect any unknown routes to login if not authenticated, or home if authenticated
  { path: '**', redirectTo: '/login' }
];
