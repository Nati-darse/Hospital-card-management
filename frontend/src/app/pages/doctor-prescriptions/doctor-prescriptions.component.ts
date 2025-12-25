import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ReactiveFormsModule, FormsModule, FormGroup, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { ApiService } from '../../services/api.service';
import { User } from '../../models/auth.models';

@Component({
    selector: 'app-doctor-prescriptions',
    standalone: true,
    imports: [CommonModule, RouterModule, ReactiveFormsModule, FormsModule],
    templateUrl: './doctor-prescriptions.component.html',
    styleUrl: './doctor-prescriptions.component.scss'
})
export class DoctorPrescriptionsComponent implements OnInit {
    currentUser: User | null = null;
    staffProfile: any = null;
    patients: any[] = [];
    selectedPatient: any = null;
    patientHistory: any[] = [];
    prescriptionForm: FormGroup;
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
        this.prescriptionForm = this.fb.group({
            patientId: ['', Validators.required],
            diagnosis: ['', Validators.required],
            prescription: ['', Validators.required],
            labTests: [''],
            followUpDate: [''],
            status: ['Stable'],
            content: [''],
            additionalComments: ['']
        });

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
            this.loadDoctorData();
        }
    }

    loadDoctorData(): void {
        if (!this.currentUser) return;

        this.apiService.get('staff').subscribe({
            next: (staffList: any[]) => {
                this.staffProfile = staffList.find(s => s.user?.id === this.currentUser?.id);
                if (this.staffProfile) {
                    this.loadPatients();
                }
            }
        });
    }

    loadPatients(): void {
        this.apiService.get(`patients?doctorId=${this.staffProfile.id}`).subscribe({
            next: (assignedPatients: any[]) => {
                this.patients = assignedPatients;
            }
        });
    }

    selectPatient(patient: any): void {
        this.selectedPatient = patient;
        this.prescriptionForm.reset({ status: 'Stable', patientId: patient.id });
        this.loadPatientHistory(patient.id);
    }

    loadPatientHistory(patientId: number): void {
        this.apiService.get(`visits/patient/${patientId}`).subscribe({
            next: (history: any[]) => {
                this.patientHistory = history.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
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

    submitPrescription(): void {
        if (this.prescriptionForm.invalid || !this.selectedPatient) return;

        this.loading = true;
        const visitData = {
            ...this.prescriptionForm.value,
            doctorId: this.staffProfile.id,
            visitDate: new Date().toISOString().split('T')[0]
        };

        this.apiService.post('visits', visitData).subscribe({
            next: () => {
                this.loading = false;
                alert('Medical record saved successfully!');
                this.prescriptionForm.reset({ status: 'Stable', patientId: this.selectedPatient.id });
                this.loadPatientHistory(this.selectedPatient.id);
            },
            error: (err) => {
                this.loading = false;
                alert(err.error?.message || 'Failed to save record.');
            }
        });
    }

    onLogout(): void {
        this.authService.logout();
        this.router.navigate(['/login']);
    }
}
