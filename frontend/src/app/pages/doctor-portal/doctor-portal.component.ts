import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ReactiveFormsModule, FormGroup, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { ApiService } from '../../services/api.service';
import { User } from '../../models/auth.models';

@Component({
    selector: 'app-doctor-portal',
    standalone: true,
    imports: [CommonModule, RouterModule, ReactiveFormsModule],
    templateUrl: './doctor-portal.component.html',
    styleUrl: './doctor-portal.component.scss'
})
export class DoctorPortalComponent implements OnInit {
    currentUser: User | null = null;
    staffProfile: any = null;
    patientsCount = 0;
    todaysVisitsCount = 0;
    recentPatients: any[] = [];
    loading = false;

    // Password Update
    showPasswordForm = false;
    passwordForm: FormGroup;
    passwordError = '';
    passwordSuccess = '';

    constructor(
        private authService: AuthService,
        private apiService: ApiService,
        private router: Router,
        private fb: FormBuilder
    ) {
        this.passwordForm = this.fb.group({
            currentPassword: ['', [Validators.required]],
            newPassword: ['', [Validators.required, Validators.minLength(6)]],
            confirmPassword: ['', [Validators.required]]
        }, { validator: this.passwordMatchValidator });
    }

    passwordMatchValidator(g: FormGroup) {
        return g.get('newPassword')?.value === g.get('confirmPassword')?.value
            ? null : { 'mismatch': true };
    }

    ngOnInit(): void {
        this.currentUser = this.authService.getCurrentUser();
        if (this.currentUser) {
            this.loadDoctorProfile();
        }
    }

    loadDoctorProfile(): void {
        this.loading = true;
        this.apiService.get('staff/me').subscribe({
            next: (profile: any) => {
                this.staffProfile = profile;
                if (this.staffProfile) {
                    this.loadDashboardData();
                }
            },
            error: () => this.loading = false
        });
    }

    loadDashboardData(): void {
        if (!this.staffProfile) return;

        // Load assigned patients count and list
        this.apiService.get(`patients?doctorId=${this.staffProfile.id}`).subscribe({
            next: (assignedPatients: any[]) => {
                this.patientsCount = assignedPatients.length;
                this.recentPatients = assignedPatients.slice(0, 5);
            }
        });

        // Load today's visits count
        this.apiService.get(`visits/doctor/${this.staffProfile.id}`).subscribe({
            next: (visits: any[]) => {
                const today = new Date().toISOString().split('T')[0];
                this.todaysVisitsCount = visits.filter(v => v.visitDate === today).length;
                this.loading = false;
            },
            error: () => this.loading = false
        });
    }

    togglePasswordForm(): void {
        this.showPasswordForm = !this.showPasswordForm;
        this.passwordError = '';
        this.passwordSuccess = '';
        if (this.showPasswordForm) {
            this.passwordForm.reset();
        }
    }

    updatePassword(): void {
        if (this.passwordForm.invalid) return;
        this.loading = true;
        this.passwordError = '';
        this.passwordSuccess = '';

        this.apiService.post('users/change-password', {
            currentPassword: this.passwordForm.value.currentPassword,
            newPassword: this.passwordForm.value.newPassword
        }).subscribe({
            next: () => {
                this.loading = false;
                this.passwordSuccess = 'Password updated successfully!';
                setTimeout(() => this.showPasswordForm = false, 2000);
            },
            error: (err) => {
                this.loading = false;
                this.passwordError = err.error?.message || 'Failed to update password. Please check your current password.';
            }
        });
    }

    onLogout(): void {
        this.authService.logout();
        this.router.navigate(['/login']);
    }
}
