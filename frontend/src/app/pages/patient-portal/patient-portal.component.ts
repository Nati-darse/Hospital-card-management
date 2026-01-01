import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ReactiveFormsModule, FormGroup, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { ApiService } from '../../services/api.service';
import { User } from '../../models/auth.models';

@Component({
    selector: 'app-patient-portal',
    standalone: true,
    imports: [CommonModule, RouterModule, ReactiveFormsModule],
    templateUrl: './patient-portal.component.html',
    styleUrl: './patient-portal.component.scss'
})
export class PatientPortalComponent implements OnInit {
    currentUser: User | null = null;
    patientProfile: any = null;
    clinicalHistory: any[] = [];
    assignedDoctor: any = null;
    loading = false;

    // Password Update
    showPasswordForm = false;
    passwordForm!: FormGroup;
    passwordError = '';
    passwordSuccess = '';

    // Prescriptions
    prescriptions: any[] = [];

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
            this.loadPatientProfile();
        }
    }

    loadPatientProfile(): void {
        this.loading = true;
        this.apiService.get('patients/me').subscribe({
            next: (profile: any) => {
                this.patientProfile = profile;
                if (profile) {
                    this.assignedDoctor = profile.assignedDoctor;
                    this.loadClinicalHistory(profile.id);
                    this.loadPrescriptions(profile.id);
                }
                this.loading = false;
            },
            error: (err) => {
                console.error('Failed to load patient profile', err);
                this.loading = false;
            }
        });
    }

    loadPrescriptions(patientId: number): void {
        this.apiService.get(`prescriptions/patient/${patientId}`).subscribe({
            next: (list: any[]) => this.prescriptions = list
        });
    }

    loadClinicalHistory(patientId: number): void {
        this.apiService.get(`visits/patient/${patientId}`).subscribe({
            next: (history: any[]) => {
                this.clinicalHistory = history.sort((a, b) =>
                    new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
                );
            }
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
                this.passwordError = err.error?.message || 'Failed to update password.';
            }
        });
    }

    onLogout(): void {
        this.authService.logout();
        this.router.navigate(['/login']);
    }
}
