import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { ReactiveFormsModule, FormsModule, FormGroup, FormBuilder, Validators } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { ApiService } from '../../services/api.service';
import { User } from '../../models/auth.models';

@Component({
    selector: 'app-doctor-patients',
    standalone: true,
    imports: [CommonModule, RouterModule, ReactiveFormsModule, FormsModule],
    templateUrl: './doctor-patients.component.html',
    styleUrl: './doctor-patients.component.scss'
})
export class DoctorPatientsComponent implements OnInit {
    currentUser: User | null = null;
    staffProfile: any = null;
    allPatients: any[] = [];
    patients: any[] = [];
    searchQuery = '';
    selectedPatient: any = null;
    loading = false;

    // Password Update
    showPasswordForm = false;
    passwordForm: FormGroup;
    passwordError = '';
    passwordSuccess = '';

    // Referral
    showReferralForm = false;
    departments: string[] = ['Cardiology', 'Neurology', 'Surgery', 'Pediatrics', 'Orthopedics', 'General Medicine'];
    referralSearchDepartment = '';
    referralDoctors: any[] = [];
    referralLoading = false;

    // Patient Update
    showUpdateForm = false;
    updateForm: FormGroup;

    constructor(
        private authService: AuthService,
        private apiService: ApiService,
        private router: Router,
        private fb: FormBuilder
    ) {
        this.updateForm = this.fb.group({
            phoneNumber: [''],
            address: [''],
            emergencyContact: ['']
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
                this.allPatients = assignedPatients;
            }
        });
    }

    onSearch(): void {
        if (!this.searchQuery.trim()) {
            this.patients = this.allPatients;
            return;
        }

        const query = this.searchQuery.toLowerCase().trim();
        this.patients = this.allPatients.filter(p =>
            p.medicalRecordNumber.toLowerCase().includes(query) ||
            p.user?.firstName.toLowerCase().includes(query) ||
            p.user?.lastName.toLowerCase().includes(query)
        );
    }

    selectPatient(patient: any): void {
        this.selectedPatient = patient;
        this.showUpdateForm = false;
        this.showReferralForm = false;

        if (patient.user) {
            this.updateForm.patchValue({
                phoneNumber: patient.user.phoneNumber,
                address: patient.user.address,
                emergencyContact: patient.emergencyContactPhone
            });
        }
    }

    toggleUpdateForm(): void {
        this.showUpdateForm = !this.showUpdateForm;
        this.showReferralForm = false;
    }

    toggleReferralForm(): void {
        this.showReferralForm = !this.showReferralForm;
        this.showUpdateForm = false;
    }

    updatePatientInfo(): void {
        if (this.updateForm.invalid || !this.selectedPatient) return;
        this.loading = true;
        const payload = { ...this.selectedPatient, ...this.updateForm.value };
        this.apiService.put(`patients/${this.selectedPatient.id}`, payload).subscribe({
            next: () => {
                this.loading = false;
                alert('Patient information updated successfully.');
                this.showUpdateForm = false;
                this.loadPatients();
            },
            error: () => this.loading = false
        });
    }

    searchDoctors(): void {
        if (!this.referralSearchDepartment) return;
        this.referralLoading = true;
        this.apiService.get(`staff/search?department=${this.referralSearchDepartment}`).subscribe({
            next: (docs: any[]) => {
                this.referralDoctors = docs.filter(d => d.id !== this.staffProfile.id);
                this.referralLoading = false;
            },
            error: () => this.referralLoading = false
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

    referPatient(toDoctor: any): void {
        if (!this.selectedPatient) return;
        if (!confirm(`Are you sure you want to refer Dr. ${toDoctor.user.firstName} ${toDoctor.user.lastName}?`)) return;

        this.loading = true;
        this.apiService.put(`patients/${this.selectedPatient.id}`, {
            ...this.selectedPatient,
            assignedDoctorId: toDoctor.id
        }).subscribe({
            next: () => {
                this.loading = false;
                alert(`Referral successful.`);
                this.selectedPatient = null;
                this.showReferralForm = false;
                this.loadPatients();
            },
            error: () => this.loading = false
        });
    }

    onLogout(): void {
        this.authService.logout();
        this.router.navigate(['/login']);
    }
}
