import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { LandingComponent } from './pages/landing/landing.component';

export const routes: Routes = [
    { path: '', component: LandingComponent },
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    {
        path: 'dashboard',
        loadComponent: () => import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent),
        canActivate: [authGuard]
    },
    {
        path: 'patient-portal',
        loadComponent: () => import('./pages/patient-portal/patient-portal.component').then(m => m.PatientPortalComponent),
        canActivate: [authGuard]
    },
    {
        path: 'doctor-portal',
        loadComponent: () => import('./pages/doctor-portal/doctor-portal.component').then(m => m.DoctorPortalComponent),
        canActivate: [authGuard]
    },
    {
        path: 'patients',
        loadComponent: () => import('./pages/patients/patients.component').then(m => m.PatientsComponent),
        canActivate: [authGuard]
    },
    {
        path: 'patient-registration',
        loadComponent: () => import('./pages/patient-registration/patient-registration.component').then(m => m.PatientRegistrationComponent),
        canActivate: [authGuard]
    },
    {
        path: 'card-management',
        loadComponent: () => import('./pages/card-management/card-management.component').then(m => m.CardManagementComponent),
        canActivate: [authGuard]
    },
    {
        path: 'staff-management',
        loadComponent: () => import('./pages/staff-management/staff-management.component').then(m => m.StaffManagementComponent),
        canActivate: [authGuard]
    },
    { path: '**', redirectTo: '/login' }
];
