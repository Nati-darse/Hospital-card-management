import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { LoginComponent } from './pages/login/login.component';
import { RegisterComponent } from './pages/register/register.component';
import { LandingComponent } from './pages/landing/landing.component';

import { roleGuard } from './guards/role.guard';

export const routes: Routes = [
    { path: '', component: LandingComponent },
    { path: 'login', component: LoginComponent },
    { path: 'register-patient', component: RegisterComponent },
    { path: 'register', component: RegisterComponent },
    {
        path: 'dashboard',
        loadComponent: () => import('./pages/dashboard/dashboard.component').then(m => m.DashboardComponent),
        canActivate: [authGuard, roleGuard],
        data: { roles: ['ADMIN'] }
    },
    {
        path: 'patient-portal',
        loadComponent: () => import('./pages/patient-portal/patient-portal.component').then(m => m.PatientPortalComponent),
        canActivate: [authGuard, roleGuard],
        data: { roles: ['PATIENT'] }
    },
    {
        path: 'doctor-portal',
        loadComponent: () => import('./pages/doctor-portal/doctor-portal.component').then(m => m.DoctorPortalComponent),
        canActivate: [authGuard, roleGuard],
        data: { roles: ['USER'] } // 'USER' is the role string for Doctor
    },
    {
        path: 'doctor-patients',
        loadComponent: () => import('./pages/doctor-patients/doctor-patients.component').then(m => m.DoctorPatientsComponent),
        canActivate: [authGuard, roleGuard],
        data: { roles: ['USER'] }
    },
    {
        path: 'doctor-prescriptions',
        loadComponent: () => import('./pages/doctor-prescriptions/doctor-prescriptions.component').then(m => m.DoctorPrescriptionsComponent),
        canActivate: [authGuard, roleGuard],
        data: { roles: ['USER'] }
    },
    {
        path: 'patients',
        loadComponent: () => import('./pages/patients/patients.component').then(m => m.PatientsComponent),
        canActivate: [authGuard, roleGuard],
        data: { roles: ['ADMIN', 'USER'] } // Both Admin and Doctor can view patients list
    },
    {
        path: 'patient-registration',
        loadComponent: () => import('./pages/patient-registration/patient-registration.component').then(m => m.PatientRegistrationComponent),
        canActivate: [authGuard, roleGuard],
        data: { roles: ['ADMIN'] } // Strict Admin only
    },
    {
        path: 'card-management',
        loadComponent: () => import('./pages/card-management/card-management.component').then(m => m.CardManagementComponent),
        canActivate: [authGuard, roleGuard],
        data: { roles: ['ADMIN'] }
    },
    {
        path: 'staff-management',
        loadComponent: () => import('./pages/staff-management/staff-management.component').then(m => m.StaffManagementComponent),
        canActivate: [authGuard, roleGuard],
        data: { roles: ['ADMIN'] } // Strict Admin only
    },
    { path: '**', redirectTo: '/login' }
];
