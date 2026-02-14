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
    selectedCaseId: number | null = null;
    assignedDoctor: any = null;
    loading = false;
    requestingAppointment = false;
    appointmentRequestMessage = '';

    // Password Update
    showPasswordForm = false;
    passwordForm!: FormGroup;
    passwordError = '';
    passwordSuccess = '';

    // Prescriptions
    prescriptions: any[] = [];

    // Mobile Menu
    mobileMenuOpen = false;

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
                this.selectedCaseId = this.clinicalHistory.length ? this.clinicalHistory[0].id : null;
            }
        });
    }

    toggleCaseDetails(caseId: number): void {
        this.selectedCaseId = this.selectedCaseId === caseId ? null : caseId;
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

    requestAppointment(): void {
        if (!this.patientProfile?.id || this.requestingAppointment) {
            return;
        }

        if (this.assignedDoctor) {
            this.appointmentRequestMessage = 'You already have an assigned doctor.';
            return;
        }

        this.requestingAppointment = true;
        this.appointmentRequestMessage = '';

        const payload = {
            reason: 'Patient requested appointment from portal',
            notes: this.assignedDoctor
                ? `Current assigned doctor: Dr. ${this.assignedDoctor.user?.firstName || ''} ${this.assignedDoctor.user?.lastName || ''}`.trim()
                : 'Patient currently has no assigned doctor.'
        };

        this.apiService.post('appointments/request', payload).subscribe({
            next: () => {
                this.requestingAppointment = false;
                this.appointmentRequestMessage = 'Appointment request submitted. Admin will assign a doctor.';
            },
            error: (err) => {
                this.requestingAppointment = false;
                this.appointmentRequestMessage = err?.error?.message || 'Failed to submit appointment request.';
            }
        });
    }

    toggleMobileMenu(): void {
        this.mobileMenuOpen = !this.mobileMenuOpen;
    }
}
